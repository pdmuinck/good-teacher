package com.pdemuinck;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ActivityMockServiceTest {

  @Test
  public void when_activity_already_exists_then_we_do_not_persist(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    activityService.addActivity("painting");
    activityService.addActivity("painting");
    Mockito.verify(dataStoreMock, Mockito.times(1)).writeActivity("painting,\r\n");
  }

}
