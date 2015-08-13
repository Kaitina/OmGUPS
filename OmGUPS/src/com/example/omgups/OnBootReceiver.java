package com.example.omgups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {
/**
 * ����������������� ��������, ����������� ������������ �������
 * ��� �������� ��������� UpdateService
 */
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, UpdateService.class);
            context.startService(serviceLauncher);
        }
    }
}
