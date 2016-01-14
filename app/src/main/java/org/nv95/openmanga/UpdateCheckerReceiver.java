package org.nv95.openmanga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.nv95.openmanga.utils.ChaptersSyncService;

/**
 * Created by nv95 on 19.12.15.
 */
public class UpdateCheckerReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ChaptersSyncService.SetScheduledStart(context);
    context.startService(new Intent(context, ChaptersSyncService.class));
  }
}
