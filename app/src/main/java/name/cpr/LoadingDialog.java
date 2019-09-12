package name.cpr;
import android.app.*;
import android.content.*;
import android.graphics.drawable.*;
import android.widget.AbsoluteLayout.*;
import android.view.*;
import android.text.*;

public class LoadingDialog{
	public static ProgressDialog dialog;
	public static Dialog bg;
	public static LoadingDialog instance;
	private LoadingDialog(Context ctx){	
		bg = new Dialog(ctx, android.R.style.ThemeOverlay_Material_Dark);
		dialog = new ProgressDialog(ctx, android.R.style.ThemeOverlay_Material_Dark);
		//dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.WHITE));
		dialog.setTitle("Loading. Please wait...");
		dialog.setMessage("Loading data!");
		dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.getWindow().setGravity(Gravity.CENTER);
		dialog.setCancelable(true);
		bg.setCancelable(true);
	}

	public static LoadingDialog getInstance(Context ctx){
		if( instance != null){
			return instance;
		}else{
			return new LoadingDialog(ctx);
		}
	}
	public static void show(Context ctx){
		if(dialog == null){
			getInstance(ctx);
		}
		if(dialog.isShowing()){
			return;
		}
		bg.show();
		dialog.show();
	}

	public static void hide(){
		if (dialog == null){
			return;
		}
		if(!dialog.isShowing()){
			return;
		}
		try{
			bg.hide();
			bg.cancel();
			dialog.hide();
		}catch(Exception ignored){}
	}
}
