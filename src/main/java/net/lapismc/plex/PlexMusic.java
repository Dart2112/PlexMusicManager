package net.lapismc.plex;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

class PlexMusic {

    private final File folder = new File("./Music");

    PlexMusic() {
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            //Rename the file to match metadata
            renameFile(f);
        }
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory())
                continue;
            File dir = generateDir(f);
            try {
                System.out.println(f.getName());
                FileUtils.moveFileToDirectory(f, dir, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void renameFile(File f) {
        if (f.isDirectory()) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                renameFile(file);
            }
            if (f.listFiles().length == 0)
                f.delete();
        }
        String fileExtension;
        if (f.getName().endsWith(".flac"))
            fileExtension = ".flac";
        else if (f.getName().endsWith(".mp3")) {
            fileExtension = ".mp3";
        } else {
            return;
        }
        try {
            File newName = new File(folder + File.separator + getTrackFileName(f) + fileExtension);
            f.renameTo(newName);
        } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException | IOException e) {
            e.printStackTrace();
        }
    }

    private File generateDir(File f) {
        return new File(folder, getArtist(f) + File.separator + getAlbum(f));
    }

    private String getTrackFileName(File f) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        AudioFile audioFile = AudioFileIO.read(f);
        Tag tag = audioFile.getTag();
        String title, artist;
        title = tag.getFirst(FieldKey.TITLE);
        artist = getArtist(f);
        if (!title.equals("")) {
            if (!artist.equals("")) {
                title = title + " - " + artist;
            }
            title = title.replaceAll("[\\\\/:*?\"<>|]", "");
            return cleanupWhitespace(title);
        } else {
            return "";
        }
    }

    private String cleanupWhitespace(String s) {
        s = s.replace("_", " ");
        while (s.startsWith(" ")) {
            s = s.substring(1);
        }
        while (s.endsWith(" ")) {
            s = s.substring(0, s.length() - 1);
        }
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        return s;
    }

    private String getArtist(File f) {
        AudioFile audioFile;
        String artist = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            if (artist.equals(""))
                artist = tag.getFirst(FieldKey.ARTIST);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        artist = artist.replaceAll("[\\/:*?\"<>|]", "");
        return !artist.equals("") ? artist : "Unknown Artist";
    }

    private String getAlbum(File f) {
        AudioFile audioFile;
        String album = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            album = tag.getFirst(FieldKey.ALBUM);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        album = album.replaceAll("[\\/:*?\"<>|]", "");
        while (album.endsWith(".")) {
            album = album.substring(0, album.length() - 1);
        }
        return !album.equals("") ? album : "Unknown Album";
    }

}
