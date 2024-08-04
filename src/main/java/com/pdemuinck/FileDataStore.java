package com.pdemuinck;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileDataStore implements DataStore {

  private final String appHome;

  public FileDataStore() {
    this(String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
        "GoodTeacher"));
  }

  public FileDataStore(String appHome) {
    this.appHome = appHome;
  }

  @Override
  public void writeActivity(String data) {
    write(data, "activities.csv", StandardOpenOption.APPEND);
  }

  @Override
  public void overWriteActivities(String data) {
    write(data, "activities.csv", StandardOpenOption.TRUNCATE_EXISTING);
  }

  private void write(String data, String file, StandardOpenOption openOption) {
    File customDir = new File(appHome);

    if (customDir.exists() || customDir.mkdirs()) {
      try {
        File out = new File(String.join(File.separator, appHome, file));
        if( out.createNewFile()){
          Files.write( out.toPath(),
              data.getBytes(StandardCharsets.UTF_8), openOption);
        } else {
          Files.write( out.toPath(),
              data.getBytes(StandardCharsets.UTF_8), openOption);
        }
      } catch (IOException e) {
        System.out.println("Failed to create file");
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(
          String.format("Cannot write data, because custom dir doesn't exist: %s", appHome));
    }
  }

  @Override
  public void saveUser(String userData) {
    String toWrite = userData + System.lineSeparator();
    File customDir = new File(appHome);

    if (customDir.exists() || customDir.mkdirs()) {
      try {
        File file = new File(String.join(File.separator, appHome, "users.csv"));
        file.createNewFile();
        Files.write(file.toPath(), toWrite.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(
          String.format("Cannot write data, because custom dir doesn't exist: %s", appHome));
    }
  }

  @Override
  public void overWriteUsers(String data) {
    write(data, "users.csv", StandardOpenOption.TRUNCATE_EXISTING);
  }

  @Override
  public List<String> fetchActivities() {
    String path = String.join(File.separator, appHome, "activities.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (NoSuchFileException e) {
      write("", "activities.csv", StandardOpenOption.APPEND);
      return new ArrayList<>();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> fetchUsers() {
    String path = String.join(File.separator, appHome, "users.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void saveActivityTime(String data) {
    write(data, "timings.csv", StandardOpenOption.APPEND);
  }

  @Override
  public List<String> fetchActivityTime() {
    String path = String.join(File.separator, appHome, "timings.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void saveBlacklist(String data) {
    write(data, "blacklist.csv", StandardOpenOption.APPEND);
  }

  @Override
  public List<String> fetchBlackLists() {
    String path = String.join(File.separator, appHome, "blacklist.csv");
    try {
      if(Files.exists(Path.of(path))){
        return Files.readAllLines(Path.of(path));
      } else {
        return new ArrayList<>();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void overwriteBlackList(String data) {
    write(data, "blacklist.csv", StandardOpenOption.TRUNCATE_EXISTING);
  }

  @Override
  public void saveFeedback(String data) {
    write(data, "feedback.csv", StandardOpenOption.APPEND);
  }
}
