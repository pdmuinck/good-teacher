package com.pdemuinck;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileDataStore implements DataStore {

  @Override
  public void writeActivity(String data) {
    write(data, "activities.csv", StandardOpenOption.APPEND);
  }

  @Override
  public void overWriteActivities(String data) {
    write(data, "activities.csv", StandardOpenOption.TRUNCATE_EXISTING);
  }

  private void write(String data, String file, StandardOpenOption openOption) {
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
        "GoodTeacher");
    File customDir = new File(path);

    if (customDir.exists() || customDir.mkdirs()) {
      try {
        Files.write(new File(String.join(File.separator, path, file)).toPath(),
            data.getBytes(StandardCharsets.UTF_8), openOption);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(
          String.format("Cannot write data, because custom dir doesn't exist: %s", path));
    }
  }

  @Override
  public void saveUser(String userData) {
    String toWrite = userData + System.lineSeparator();
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
        "GoodTeacher");
    File customDir = new File(path);

    if (customDir.exists() || customDir.mkdirs()) {
      try {
        File file = new File(String.join(File.separator, path, "users.csv"));
        file.createNewFile();
        Files.write(file.toPath(), toWrite.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException(
          String.format("Cannot write data, because custom dir doesn't exist: %s", path));
    }
  }

  @Override
  public void overWriteUsers(String data) {
    write(data, "users.csv", StandardOpenOption.TRUNCATE_EXISTING);
  }

  @Override
  public List<String> fetchActivities() {
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
        "GoodTeacher", "activities.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> fetchUsers() {
    String path = String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
        "GoodTeacher", "users.csv");
    try {
      return Files.readAllLines(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
