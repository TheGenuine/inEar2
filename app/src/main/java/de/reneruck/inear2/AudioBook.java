package de.reneruck.inear2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import de.reneruck.inear2.db.AsyncGetBookmark;
import de.reneruck.inear2.db.DatabaseManager;
import de.reneruck.inear2.file.FileScanner;

public class AudioBook {

	private int id;
	private String name;
	private String author;
	private List<String> playlist;
	private int track = 0;
	private Bookmark bookmark;

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private DatabaseManager databaseManager;

	public AudioBook(String name) {
		this.id = name.hashCode();
		this.name = name;
	}

	public AudioBook withPlaylist(List<String> strings) {
		this.playlist = strings;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPlaylist() {
		return playlist;
	}

	public void setPlaylist(List<String> playlist) {
		this.playlist = playlist;
	}

	public int getCurrentTrack() {
		return track;
	}

	public void setCurrentTrack(int track) {
		int oldTrack = this.track;
		this.track = track;
		this.changes.firePropertyChange("track", oldTrack, track);
	}

	public Bookmark getBookmark() {
		return bookmark;
	}

	public void setBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
        this.track = bookmark.getTrackNumber();
	}

	public void setPreviousTrack() throws PlaylistFinishedException {
		if (this.track - 1 >= 0) {
			int oldTrack = this.track;
			this.track--;
			if(this.playlist.get(this.track).equals(FileScanner.END_OF_CD)) {
				setPreviousTrack();
			} else {
				this.changes.firePropertyChange("track", oldTrack, this.track);
			}
		} else {
			throw new PlaylistFinishedException();
		}
	}

	public void setNextTrack() throws PlaylistFinishedException {
		if (this.track + 1 <= this.playlist.size()-1) {
			int oldTrack = this.track;
			this.track++;
			if(this.playlist.get(this.track).equals(FileScanner.END_OF_CD)) {
				setNextTrack();
			} else {
				this.changes.firePropertyChange("track", oldTrack, this.track);
			}
		} else {
			throw new PlaylistFinishedException();
		}
	}

	public void loadStoredBookmark() {
        if(bookmark != null){
            AsyncGetBookmark getBookmarkTask = new AsyncGetBookmark(this.databaseManager);
            getBookmarkTask.execute(this);
            getBookmarkTask.listenWith(new ListenableAsyncTask.AsyncTaskListener<Bookmark>() {
                @Override
                public void onPostExecute(Bookmark bookmark) {
                    if(bookmark != null)
                    {
                        setBookmark(bookmark);
                        setCurrentTrack(bookmark.getTrackNumber());
                    }
                }
            });
        }
	}

	public String getCurrentTrackName() {
		String[] split = this.playlist.get(this.track).split("/");
		return split[split.length-1].replace(".mp3", "").trim();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		this.changes.removePropertyChangeListener(l);
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
