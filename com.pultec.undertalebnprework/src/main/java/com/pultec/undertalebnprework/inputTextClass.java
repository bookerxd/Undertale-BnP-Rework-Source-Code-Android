/****************************************************************************************************************************

inputTextEx extension by Mattia Fortunati

this extension will open a popup for requesting the user to input some text
and then call a social async event into Game Maker passing the input text
can be used for getting usernames, promo codes, numbers etc etc

Usage:

simply call showInputText(String strTit,String strYes,String strNo) from game maker
for opening the popup. 
Example:
showInputText("Title Text", "OK", "Cancel")

and, in a social async event put some code like this:
var type = string(async_load[? "type"])
var data = string(async_load[? "data"])

//check for INPUTTEXT type of event
if type == "INPUTTEXT"
{
    logToConsole("Input Text Received: "+data)
    //example check code received to unlock reward
    if (data == "DATAIDTOUNLOCKREWARD"){
        //unlock reward
    }
}


"type" passed will be "INPUTTEXT" 
"data" passed will be the text input by the user

NOTE: the keyboard won't never go fullscreen, and the function both works for PORTRAIT and LANDSCAPE orientations


Author: Mattia Fortunati
Contact: mattia@mattiafortunati.com
Website: http://www.mattiafortunati.com

****************************************************************************************************************************/


package com.pultec.undertalebnprework;

//Basic imports
import android.util.Log;
import java.lang.String;
//
import android.app.Activity;
import android.content.Intent;

//Import Game Maker classes
import com.pultec.undertalebnprework.R;
import com.yoyogames.runner.RunnerJNILib;
import com.pultec.undertalebnprework.RunnerActivity;
//
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.EditText;
import android.text.InputType;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;



public class inputTextClass extends Activity {

private static String strOk;
private static String strCancel;
private static String strTitle;
private static final int EVENT_OTHER_SOCIAL = 70;


public void showInputText(String strTit,String strYes,String strNo)
{
    strTitle = strTit;
    strOk = strYes;
    strCancel = strNo;
    Intent intent = new Intent(RunnerActivity.CurrentActivity, inputTextClass.class);
    RunnerActivity.CurrentActivity.startActivity(intent);
}


@Override
    protected void onStart()
    {
        super.onStart();  
        Log.i("yoyo", "Showing inputTextClass Activity");
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(strTitle);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        //REALLY IMPORTANT: TO MAKE IT WORK ALSO IN LANDSCAPE MODE
        //AND NOT GOING FULL SCREEN!
        input.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);

        builder.setCancelable(false);
        // Set up the buttons
        builder.setPositiveButton(strOk, new DialogInterface.OnClickListener() { 
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ReturnAsync("INPUTTEXT",input.getText().toString());
            //
            finish();
        }
        });

    //dialog for showing keyboard froms start
    final AlertDialog dialog = builder.create();
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
    @Override
    public void onDismiss(final DialogInterface dialog) {
        //
        finish();
    }
    });

    dialog.show();
}



     public void ReturnAsync(String tp, String txt)
{
        int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
        RunnerJNILib.DsMapAddString( dsMapIndex, "type", tp );
        RunnerJNILib.DsMapAddString( dsMapIndex, "data", txt );
        RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
  }

}
