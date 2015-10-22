package club.easley.musiquik;

import android.content.Context;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;

import org.jsoup.Connection;

/**
 * Created by measley on 10/4/2015.
 */
public class FlycoMenuDialog {

    BaseAnimatorSet bas_in;
    BaseAnimatorSet bas_out;
    Context context;
    String title;
    String content;
    String btnText;

    public FlycoMenuDialog(Context context, BaseAnimatorSet bi, BaseAnimatorSet  bo, String title, String content, String btnText){
        this.bas_in = bi;
        this.bas_out = bo;
        this.context = context;
        this.title = title;
        this.content = content;
        this.btnText = btnText;

    }

    public void showMaterialDialog(){
        final MaterialDialog dialog = new MaterialDialog(context);
        dialog
                .content(content)
                .title(title)
                .titleTextColor(context.getResources().getColor(R.color.md_blue_800))
                .btnNum(1)

                .btnText(btnText)
                .btnTextColor(context.getResources().getColor(R.color.md_blue_800))
                .showAnim(bas_in)
                .dismissAnim(bas_out)
                .show();

        dialog.setCanceledOnTouchOutside(false);




        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        });


    }
}
