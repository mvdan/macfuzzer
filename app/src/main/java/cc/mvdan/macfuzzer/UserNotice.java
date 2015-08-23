/*
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 * Copyright 2015 Daniel Martí <mvdan@mvdan.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cc.mvdan.macfuzzer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.Toast;

public class UserNotice {
    private Activity mAct;

    public UserNotice(Activity act) {
        mAct = act;
    }

    public void showSuggestRestartAlert(String tag) {
        launchAndCloseAlert(R.string.notice_restart,
                                   R.string.notice_restart_title,
                                   R.string.button_close_app,
                                   R.string.button_continue,
                                   "closeapp", "do_nothing",
                                   tag);
    }

    public void showInvalidEntryAlert(int messageId, String tag) {
        launchAndCloseAlert(
                                messageId,
                                R.string.manualmac_notice_invalid_title,
                                R.string.button_okay,
                                0, "do_nothing", "do_nothing",
                                tag);
    }

    public void launchAndCloseAlert(int messageId, int titleId,
                                    int pbutton, int nbutton,
                                    String cbPos, String cbNeg,
                                    String tag) {
        DialogFragment dialog =
                new AlertNoticeDialogFragment(messageId, titleId,
                                              pbutton, nbutton,
                                              cbPos, cbNeg);
        dialog.show(mAct.getFragmentManager(), tag);
    }

    public void launchAndCloseAlert(CharSequence msg,
                                    CharSequence title,
                                    int pbutton, int nbutton,
                                    String cbPos, String cbNeg,
                                    String tag) {
        DialogFragment dialog =
                new AlertNoticeDialogFragment(msg, title,
                                              pbutton, nbutton,
                                              cbPos, cbNeg);
        dialog.show(mAct.getFragmentManager(), tag);
    }


    public void callbackActions(String cb) {
        if (cb == "closeapp") {
            mAct.setResult(Main.RESULT_EXIT);
            mAct.finish();
        } else if (cb == "do_nothing") {
            return;
        } else {
            return;
        }
    }

    private class AlertNoticeDialogFragment extends DialogFragment {
        int mId, tId, mPosButton, mNegButton;
        String callbackPos, callbackNeg;
        CharSequence mMsg = null, mTitle = null;

        public AlertNoticeDialogFragment(int messageId, int titleId,
                                         int pbutton, int nbutton,
                                         String cbPos, String cbNeg) {
            super();
            mId = messageId;
            tId = titleId;
            mPosButton = pbutton;
            mNegButton = nbutton;
            callbackPos = cbPos;
            callbackNeg = cbNeg;
        }

        public AlertNoticeDialogFragment(CharSequence msg,
                                         CharSequence title,
                                         int pbutton, int nbutton,
                                         String cbPos, String cbNeg) {
            super();
            mMsg = msg;
            mTitle = title;
            mPosButton = pbutton;
            mNegButton = nbutton;
            callbackPos = cbPos;
            callbackNeg = cbNeg;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder =
                                 new AlertDialog.Builder(mAct);
            if (mMsg == null) {
                builder.setMessage(mId);
            } else {
                builder.setMessage(mMsg);
            }

            if (mTitle == null) {
                builder.setTitle(tId);
            } else {
                builder.setTitle(mTitle);
            }

            if (mNegButton != 0) {
                builder.setNegativeButton(mNegButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackNeg);
                    }
                });
            }
            if (mPosButton != 0) {
                builder.setPositiveButton(mPosButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackPos);
                    }
                });
            }
            return builder.create();
        }
    }

    public void makeMeChangeStatusToast(String msg, int duration) {
        Toast.makeText(mAct.getApplicationContext(),
                       msg, duration).show();
    }

    public void makeMeChangeStatusToast(int resId, int duration) {
        Toast.makeText(mAct.getApplicationContext(),
                       resId, duration).show();
    }

}
