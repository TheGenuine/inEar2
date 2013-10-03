package de.reneruck.inear2.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import de.reneruck.inear2.AppContext;

public class FileScanner extends AsyncTask<Void, Void, Void> {

	public static final String END_OF_CD = "endOfTheCD";

	private static final String TAG = "Inear2 - FileScanner";

	private AppContext appContext;

	private Comparator<String> alphaNumComp;
	
	public FileScanner(AppContext appContext) {
		super();
		this.appContext = appContext;
		this.alphaNumComp = new AlphanumComparator();
	}

	@Override
	public Void doInBackground(Void... params) {
		File baseDir = new File(this.appContext.getAudiobokkBaseDir());
		if(baseDir != null && baseDir.exists())
		{
			List<File >audiobookDirs = Arrays.asList(baseDir.listFiles());
			if(audiobookDirs.size() > 0)
			{
				for (File audiobookDir : audiobookDirs) {
					if(!hasPlaylist(audiobookDir))
					{
						createCompletePlaylist(audiobookDir);
					}
				}
			}
		}
		return null;
	}

	private void createCompletePlaylist(File audiobookDir) {
		File[] filesInAudiobookDir = audiobookDir.listFiles(dirFilter);
		List<String> mediaFiles = new LinkedList<String>();
		mediaFiles.addAll(getMediafiles(audiobookDir));
		
		if(this.appContext.getSettings().isCreateNoMediaFile()) createNoMediaFile(audiobookDir);
		
		for (File subDir : filesInAudiobookDir) {
			mediaFiles.addAll(getMediafiles(subDir));
			addEndOfCdTag(mediaFiles);
			if(this.appContext.getSettings().isCreateNoMediaFile()) createNoMediaFile(subDir);
		}
		removeLastEndOfCdTag(mediaFiles);
		createAndWritePlaylist(audiobookDir, mediaFiles);
	}

	private void addEndOfCdTag(List<String> mediaFiles) {
		mediaFiles.add(END_OF_CD);
	}
	
	private void removeLastEndOfCdTag(List<String> mediaFiles) {
		if(mediaFiles.size() >= 1) {
			if(mediaFiles.get(mediaFiles.size()-1).equals(END_OF_CD)) mediaFiles.remove(mediaFiles.size() -1);
		}
	}

	private void createNoMediaFile(File dir) {
		File noMediaFile = new File(dir.getAbsolutePath() + File.separator + ".nomedia");
		if(noMediaFile == null | !noMediaFile.exists()){
			try {
				noMediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createAndWritePlaylist(File audiobookDir, List<String> mediaFiles) {
		File playlist = new File(audiobookDir.getAbsolutePath() + File.separator + audiobookDir.getName() + ".m3u");
		try {
			playlist.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(playlist);
			for (String mediafile : mediaFiles) {
				writeEntryToPlaylist(outputStream, mediafile);
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void writeEntryToPlaylist(FileOutputStream outputStream, String currentFile) throws IOException {
		String entryText = currentFile + "\r\n";
		outputStream.write(entryText.getBytes(Charset.forName("UTF-8")));
	}
	
	private List<String> getMediafiles(File dir) {
		File[] medFiles = dir.listFiles(mediaFileFilter);
		List<String> result = new LinkedList<String>();
		if(medFiles != null) {
			for (int i = 0; i < medFiles.length; i++) {
				result.add(medFiles[i].getAbsolutePath());
			}
			Collections.sort(result, this.alphaNumComp);
		}
		return result;
	}

	private boolean hasPlaylist(File audiobookDir) {
		String[] list = audiobookDir.list(m3uFilter);
		return list != null && list.length > 0;
	}
	
	private static FilenameFilter m3uFilter = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".m3u");
		}
	};
	
	private static FileFilter dirFilter = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	
	private static FilenameFilter mediaFileFilter = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String filename) {
			return isMediaFile(filename);
		}
	};
	
	private static boolean isMediaFile(String filename) {
		String[] suffixe = new String[] {".mp3",".ogg",".wav", ".mp4" ,".aac",".m4a", ".imy",".ota",".rtttl",".rtx",".mid",".xmf",".mxmf",".flac",".3gp"};
		for (int i = 0; i < suffixe.length; i++) {
			String lowerCase = filename.toLowerCase();
			String string = suffixe[i];
			boolean endsWith = lowerCase.endsWith(string);
			if(endsWith)
			{
				return true;
			}
		}
		return false;
	}
}
