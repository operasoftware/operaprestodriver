package com.opera.core.systems.scope.services.ums;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DialogManagerCommand;
import com.opera.core.systems.scope.protos.DialogManagerProtos.ClickArg;
import com.opera.core.systems.scope.protos.DialogManagerProtos.DialogID;
import com.opera.core.systems.scope.protos.DialogManagerProtos.DialogList;
import com.opera.core.systems.scope.protos.DialogManagerProtos.ListDialogsArg;
import com.opera.core.systems.scope.protos.DialogManagerProtos.SelectArg;
import com.opera.core.systems.scope.protos.DialogManagerProtos.TypeArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IDialogManager;

public class DialogManager extends AbstractService implements IDialogManager {

  public DialogManager(ScopeServices services, String version) {
    super(services, version);
    String serviceName = "dialog-manager";

    if (!isVersionInRange(version, "1.0", serviceName)) {
      throw new UnsupportedOperationException(serviceName + " version " +
          version + " is not supported");
    }

    services.setDialogManager(this);
  }


  public DialogList ListDialogs(Integer windowId) {
    ListDialogsArg.Builder builder = ListDialogsArg.newBuilder();
    if (windowId != null) {
      builder.setWindowID(windowId);
    }

    Response response = executeCommand(DialogManagerCommand.LIST_DIALOGS, builder);

    DialogList.Builder list = DialogList.newBuilder();
    buildPayload(response, list);

    return list.build();
  }

  public void Click(DialogID dialogId, int buttonId) {
    ClickArg.Builder builder = ClickArg.newBuilder();
    builder.setDialogID(dialogId);
    builder.setButtonID(buttonId);

    Response response = executeCommand(DialogManagerCommand.CLICK, builder);

    if (response == null) {
      throw new WebDriverException("Internal error clicking a dialog button");
    }
  }

  public void Select(DialogID dialogId, int selectionId, int[] indexList) {
    SelectArg.Builder builder = SelectArg.newBuilder();
    builder.setDialogID(dialogId);
    builder.setSelectionID(selectionId);
    for (int i = 0; i < indexList.length; i++) {
      builder.setIndexList(i, indexList[i]);
    }

    Response response = executeCommand(DialogManagerCommand.SELECT, builder);

    if (response == null) {
      throw new WebDriverException("Internal error selecting a dialog element");
    }
  }

  public void Type(DialogID dialogId, int textFieldId, String text) {
    TypeArg.Builder builder = TypeArg.newBuilder();
    builder.setDialogID(dialogId);
    builder.setTextFieldID(textFieldId);
    builder.setText(text);

    Response response = executeCommand(DialogManagerCommand.TYPE, builder);

    if (response == null) {
      throw new WebDriverException("Internal error typing in a dialog");
    }
  }

}
