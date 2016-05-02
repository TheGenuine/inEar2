package de.reneruck.inear2.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import de.reneruck.inear2.AppContext;
import de.reneruck.inear2.AudioBook;
import de.reneruck.inear2.ListenableAsyncTask;

public class FileScanner extends ListenableAsyncTask<Void, Void, List<AudioBook>> {

	public static final String END_OF_CD = "endOfTheCD";

	private static final String TAG = "Inear2 - FileScanner";

	private AppContext appContext;
	private ContentResolver mContentResolver;

	String[] MIME_TYPES = new String[] {"audio/mpeg"};


	public FileScanner(AppContext appContext) {
		super();
		this.appContext = appContext;
		this.mContentResolver = this.appContext.getContentResolver();
	}

	@Override
	public List<AudioBook> doInBackground(Void... params) {
		// Check for the freshest data.
		Uri baseDir = Uri.parse(this.appContext.getAudiobookBaseDir());

		mContentResolver.takePersistableUriPermission(baseDir, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

		Uri docUri = DocumentsContract.buildDocumentUriUsingTree(baseDir, DocumentsContract.getTreeDocumentId(baseDir));

		List<AudioBook> audioBooks = new ArrayList<>();

		// Collect all audiobooks for all the base folders given
		Cursor docCursor = mContentResolver.query(docUri, new String[]{
				DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
		try {
			if (docCursor != null && docCursor.moveToFirst()) {
				audioBooks = collectValidAudioBooks(baseDir);

			}
		} finally {
			closeQuietly(docCursor);
		}
		return audioBooks;
	}

	private List<AudioBook> collectValidAudioBooks(Uri baseDir) {

		List<AudioBook> audioBooks = new ArrayList<>();

		Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseDir, DocumentsContract.getTreeDocumentId(baseDir));
		Cursor childCursor = mContentResolver.query(childrenUri,
				null,
				DocumentsContract.Document.COLUMN_MIME_TYPE + " = ?", new String[]{DocumentsContract.Document.MIME_TYPE_DIR}, //"audio/mpegurl"
				null);
		try {
			if (childCursor != null && childCursor.moveToFirst()) {
				while (childCursor.moveToNext()) {
                    String documentName = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
					String documentId = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
					String documentMineType = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));

					Uri audioBookUri = DocumentsContract.buildDocumentUriUsingTree(baseDir, documentId);

					Log.d(TAG, "AudioBook found name=" + documentName);

					if(documentMineType.equals(DocumentsContract.Document.MIME_TYPE_DIR)){
						Log.d(TAG, documentName + " is a dir");
						Uri audioBookChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseDir, documentId);

						AudioBook book = new AudioBook(documentName)
								.withPlaylist(deepCollectFilePaths(audioBookChildrenUri, Arrays.asList(MIME_TYPES)));
						audioBooks.add(book);

//						if(!hasPlaylist(audioBookChildrenUri)){
//							Log.d(TAG, documentName + " has no playlist");
//							if(createPlaylist(audioBookChildrenUri)){
//								Log.d(TAG, "Playlist created for " + documentName);
//
//							}
//						} else {
//							Log.d(TAG, documentName + " has playlist - Done");
//							audiobookDirs.add(audioBookUri);
//						}
					}
                }
//                if(this.appContext.getSettings().isCreateNoMediaFile()) createNoMediaFile(baseDir);
            }
        } finally {
			closeQuietly(childCursor);
		}
		return audioBooks;
	}

	private boolean createPlaylist(Uri audioBookChildren) {


		List<String> filesPaths = deepCollectFilePaths(audioBookChildren, Arrays.asList(MIME_TYPES));
		Log.d(TAG, "Done collecting media files, found " + filesPaths.size() + " in " + audioBookChildren);
		return !filesPaths.isEmpty() && createAndWritePlaylist(audioBookChildren, removeLastEndOfCdTag(filesPaths));
	}

	/**
	 * Traverses the directory given in inputDir and all it's sub directories
	 * collecting all files matching the given mimeTypes.
	 * @param inputDir URI to directory to scan.
	 * @param mimeTypes List<String> of mime types to collect.
     * @return List<String> of absolute file paths of files matching the criteria.
     */
	private List<String> deepCollectFilePaths(Uri inputDir, List<String> mimeTypes) {
		List<String> filePaths = new ArrayList<>();
		Log.d(TAG, "Collecting " + inputDir);

		Cursor childCursor = mContentResolver.query(inputDir,null,null,null,null);

		try {
			while(childCursor != null && childCursor.moveToNext()){
				String name = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
				String mimeType = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
				String fileId = childCursor.getString(childCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(inputDir, fileId);

//				Log.d(TAG, "found media file, name= " + name +", mime=" + mimeType);

				if(mimeTypes.contains(mimeType)) {
//					Log.d(TAG, name + " is " + mimeType + " - Done");
                    filePaths.add(getFilePath(fileUri));
				}

				if(mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)){
//					Log.d(TAG, name + " is dir, collecting");
					Uri subDocuments = DocumentsContract.buildChildDocumentsUriUsingTree(fileUri, fileId);
                    filePaths.addAll(deepCollectFilePaths(subDocuments, mimeTypes));
				}
			}
		} finally {
			closeQuietly(childCursor);
		}
		Log.d(TAG, "Collected " + filePaths.size() + " media files in " + inputDir);
//		if(this.appContext.getSettings().isCreateNoMediaFile()) createNoMediaFile(inputDir);
		addEndOfCdTag(filePaths);
		return filePaths;
	}

	private String getFilePath(Uri fileUri) {
		// ExternalStorageProvider
		final String docId = DocumentsContract.getDocumentId(fileUri);
		final String[] split = docId.split(":");
		final String type = split[0];

		return isExternalStorageDocument(fileUri) ? "/mnt/extSdCard/" + split[1] : Environment.getExternalStorageDirectory() + "/" + split[1];
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	private void addEndOfCdTag(List<String> mediaFiles) {
		mediaFiles.add(END_OF_CD);
	}

	private List<String> removeLastEndOfCdTag(List<String> mediaFiles) {
		if(!mediaFiles.isEmpty()) {
			if(mediaFiles.get(mediaFiles.size()-1).equals(END_OF_CD)) mediaFiles.remove(mediaFiles.size() -1);
		}
		return mediaFiles;
	}

	private boolean createNoMediaFile(Uri dir) {
		Log.d(TAG, "Creating .nomedia file in " + dir);
		Uri noMediaFile = DocumentsContract.createDocument(mContentResolver, dir, "nomedia", ".nomedia");
		return noMediaFile != null;
	}

	private boolean createAndWritePlaylist(Uri audiobookDir, List<String> mediaFiles) {

		Uri playlistUri = DocumentsContract.createDocument(mContentResolver, audiobookDir, "audio/mpegurl", "playlist.m3u");
		try {
			OutputStream outputStream = mContentResolver.openOutputStream(playlistUri);
			for (String mediafile : mediaFiles) {
				writeEntryToPlaylist(outputStream, mediafile);
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeEntryToPlaylist(OutputStream outputStream, String currentFile) throws IOException {
		String entryText = currentFile + "\r\n";
		outputStream.write(entryText.getBytes(Charset.forName("UTF-8")));
	}
	

	private boolean hasPlaylist(Uri audiobookDir) {
		Cursor docCursor = mContentResolver.query(audiobookDir,
				new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE},
				DocumentsContract.Document.COLUMN_MIME_TYPE + "=?", new String[]{"audio/mpegurl"}, null);
        try{
            while(docCursor != null && docCursor.moveToNext()){
                if(Objects.equals(docCursor.getString(docCursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)), "audio/mpegurl")) return true;
            }
            return false;
        } finally {
            closeQuietly(docCursor);
        }
    }
	
	public void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (RuntimeException rethrown) {
				throw rethrown;
			} catch (Exception ignored) {
			}
		}
	}
}
