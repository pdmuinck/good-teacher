package com.pdemuinck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassroomMother {


  private ClassroomMother(){};

  public static Builder classroom(){
    return new Builder();
  }

  public static class Builder {
    private List<User> kidProfiles = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private int kids = 0;

    public Builder withKids(int kids){
      this.kids = kids;
      return this;
    }

    public Builder withActivities(Consumer<ClassroomActivityMother.Builder>... activityConsumers){
      Arrays.stream(activityConsumers).forEach(cs -> {
        ClassroomActivityMother.Builder builder = ClassroomActivityMother.activity();
        cs.accept(builder);
        activities.add(builder.build());
      });
      return this;
    }

    public Classroom build(){
      if(kidProfiles.isEmpty()){
        List<User> profiles = IntStream.range(0, kids)
            .mapToObj(i -> new User(String.valueOf(i), String.valueOf(i)))
            .collect(
                Collectors.toList());
        Classroom classroom = new Classroom(profiles);
        activities.forEach(activity -> classroom.addActivity(activity));
        return classroom;
      } else {
        Classroom classroom = new Classroom(kidProfiles);
        activities.forEach(activity -> classroom.addActivity(activity));
        return classroom;
      }
    }
  }
}
