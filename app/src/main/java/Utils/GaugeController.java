package Utils;

import android.widget.ImageView;
import android.widget.TextView;

import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScGauge;

public class GaugeController {

    public static void updateGauge(ScArcGauge gauge, ImageView indicator, TextView counter){
        indicator.setPivotX(30f);
        indicator.setPivotY(30f);

        gauge.bringOnTop(ScGauge.NOTCHES_IDENTIFIER);
        gauge.setHighValue(60);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning) {

            }
        });
    }
}
