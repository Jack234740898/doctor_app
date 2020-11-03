package com.sftelehealth.doctor.video.view.call;

public class ActionHandler {
    private static ActionHandler actionHandler;
    private OnActionClickListener actionListener;

    public static ActionHandler getInstance() {
        if (actionHandler == null) {
            actionHandler = new ActionHandler();
        }

        return actionHandler;
    }

    public void setActionListener(OnActionClickListener listener){
        actionListener = listener;
    }

    public OnActionClickListener getActionListener() {
        return actionListener;
    }

}
