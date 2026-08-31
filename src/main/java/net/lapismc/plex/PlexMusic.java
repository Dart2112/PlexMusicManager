package net.lapismc.plex;

import org.apache.commons.io.FileExistsException;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
            if (f.getName().endsWith(".lrc") || f.getName().endsWith(".txt"))
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
            moveLyrics(f, fileExtension, newName);
            f.renameTo(newName);
        } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException |
                 IOException e) {
            e.printStackTrace();
        }
    }

    private void moveLyrics(File track, String fileExtension, File trackNewName) throws IOException {
        String[] exts = {".lrc", ".txt"};
        for (String ext : exts) {
            //Check if there is a lyrics file with the same name
            File lrc = new File(track.getParentFile(), track.getName().replace(fileExtension, ext));
            if (lrc.exists()) {
                System.out.println(ext + " exists at " + lrc.getPath() + ", moving to " + generateDir(track).getPath());
                File newLrc = new File(lrc.getParentFile(), trackNewName.getName().replace(fileExtension, ext));
                lrc.renameTo(newLrc);
                if (lrc.getParentFile().getPath().equals(generateDir(track).getPath()))
                    continue;
                try {
                    FileUtils.moveFileToDirectory(newLrc, generateDir(track), true);
                } catch (FileExistsException ignored) {
                    //The file didn't need to be moved or already exists, we can ignore this error
                }
            }
        }
    }

    private File generateDir(File f) {
        return new File(folder, getAlbumArtist(f) + File.separator + getAlbum(f));
    }

    private String getTrackFileName(File f) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        AudioFile audioFile = AudioFileIO.read(f);
        Tag tag = audioFile.getTag();
        String title, trackNoString, totalDiscs, discNumber;
        title = tag.getFirst(FieldKey.TITLE);
        trackNoString = tag.getFirst(FieldKey.TRACK);
        totalDiscs = tag.getFirst(FieldKey.DISC_TOTAL);
        discNumber = tag.getFirst(FieldKey.DISC_NO);
        if (!title.isEmpty()) {
            if (!trackNoString.isEmpty()) {
                int trackNo = Integer.parseInt(trackNoString);
                String paddedTrackNo = String.format("%02d", trackNo);
                title = paddedTrackNo + ". " + title;
            }
            //If there is more than one disc
            if (!totalDiscs.isEmpty() && !totalDiscs.equals("1")) {
                //Get the disc number for this track and append it to the track no
                int discNo = Integer.parseInt(discNumber);
                title = discNo + "-" + title;
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

    private String getAlbumArtist(File f) {
        AudioFile audioFile;
        String artist = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            if (artist.equals(""))
                artist = tag.getFirst(FieldKey.ARTIST);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException |
                 InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        artist = artist.replaceAll("[\\/:*?\"<>|]", "");
        artist = UTF8Format(artist);
        return !artist.equals("") ? artist : "Unknown Artist";
    }

    private String getTrackArtist(File f) {
        AudioFile audioFile;
        String artist = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            artist = tag.getFirst(FieldKey.ARTIST);
            if (artist.equals(""))
                artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException |
                 InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        artist = artist.replaceAll("[\\/:*?\"<>|]", "");
        artist = UTF8Format(artist);
        return !artist.equals("") ? artist : "Unknown Artist";
    }

    private String getAlbum(File f) {
        AudioFile audioFile;
        String album = "";
        try {
            audioFile = AudioFileIO.read(f);
            Tag tag = audioFile.getTag();

            album = tag.getFirst(FieldKey.ALBUM);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException |
                 InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        album = album.replaceAll("[\\/:*?\"<>|]", "");
        while (album.endsWith(".") || album.endsWith(" ")) {
            album = album.substring(0, album.length() - 1);
        }
        album = UTF8Format(album);
        return !album.equals("") ? album : "Unknown Album";
    }

    private String UTF8Format(String input) {
        Charset charset = StandardCharsets.UTF_8;
        return charset.decode(charset.encode(input)).toString();
    }

}
