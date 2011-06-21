/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.protos.DialogManagerProtos.DialogID;
import com.opera.core.systems.scope.protos.DialogManagerProtos.DialogList;

public interface IDialogManager {

  /**
   * List the dialogs for a specified window, or if no window is specified,
   * list *all* windows.
   *
   * @param windowId The id of the window to list dialogs from
   * @return A list of dialogs
   */
  public DialogList ListDialogs(Integer windowId);

  /**
   * Type some text into a TextField.
   *
   * @param dialogId Which Dialog that contains the TextField we wish to type in.
   * @param textFieldId Which TextField to type this text in.
   * @param text The text to type into the field.
   */
  public void Type(DialogID dialogId, int textFieldId, String text);

  /**
   * Click a Button with the specified type.
   *
   * @param dialogId Which Dialog that contains the Button we wish to click.
   * @param buttonId Which Button to click.
   */
  public void Click(DialogID dialogId, int buttonId);

  /**
   * Select an option in a Selection.
   *
   * @param dialogId Which `Dialog` that contains the `Selection` we want to interact with.
   * @param selectionId Which Selection we would like to interact with.
   * @param indexList Indices for the new set of options to be selected.
   */
  public void Select(DialogID dialogId, int selectionId, int[] indexList);

}
