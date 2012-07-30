/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Platform.MAC;
import static org.openqa.selenium.Platform.WINDOWS;

public class MouseTest extends OperaDriverTestCase {

  public static final String SENTENCE_TEXT = "This is a sentence.";
  public static final String PARAGRAPH_TEXT = "This is a paragraph." + " " + SENTENCE_TEXT;

  public OperaWebElement paragraph;
  public OperaWebElement sentence;
  public OperaWebElement outside;
  public OperaWebElement test;
  public String testHash;

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.mouse);

    paragraph = (OperaWebElement) driver.findElement(By.id("paragraph"));
    sentence = (OperaWebElement) driver.findElement(By.id("sentence"));
    outside = (OperaWebElement) driver.findElement(By.id("outside"));
    test = (OperaWebElement) driver.findElement(By.id("test"));
    testHash = test.getImageHash();

    assertEquals(0, getMonitor().outs());
    assertEquals(0, getMonitor().overs());
    assertEquals(0, getMonitor().downs());
  }

  @Test
  public void mouseMoveIsRecognizedVisually() {
    getMouse().mouseMove(test.getCoordinates());
    assertNotSame(testHash, test.getImageHash());
  }

  @Test
  public void mouseMove() {
    getMouse().mouseMove(test.getCoordinates());
    assertEquals(1, getMonitor().overs());
  }

  @Test
  public void mouseMoveByOffsetIsRecognizedVisually() {
    getMouse().mouseMove(test.getCoordinates(), 105, 105);
    assertTrue("Hashes to be same", test.getImageHash().equals(testHash));
  }

  @Test
  public void mouseMoveOutIsRecognizedVisually() {
    getMouse().mouseMove(test.getCoordinates());
    getMouse().mouseMove(outside.getCoordinates());
    assertTrue("Hashes to be same", test.getImageHash().equals(testHash));
  }

  @Test
  public void mouseMoveOut() {
    getMouse().mouseMove(test.getCoordinates());
    getMouse().mouseMove(outside.getCoordinates());
    assertEquals(1, getMonitor().overs());
    assertEquals(1, getMonitor().outs());
  }

  @Test
  public void mouseDown() {
    getMouse().mouseDown(test.getCoordinates());
    assertEquals(1, getMonitor().downs());
    assertEquals(0, getMonitor().ups());
    assertEquals(0, getMonitor().leftClicks());
  }

  @Test
  public void mouseUp() {
    getMouse().mouseDown(test.getCoordinates());
    getMouse().mouseUp(test.getCoordinates());

    assertEquals(1, getMonitor().downs());
    assertEquals(1, getMonitor().ups());
    assertEquals(1, getMonitor().leftClicks());
  }

  @Test
  public void click() {
    getMouse().click(test.getCoordinates());
    assertEquals(1, getMonitor().downs());
    assertEquals(1, getMonitor().ups());
    assertEquals(1, getMonitor().leftClicks());
  }

  @Test
  @Ignore(products = MOBILE,
          platforms = {MAC, WINDOWS},
          value = "Need investigation, probably related to OS native events")
  public void doubleClick() {
    getMouse().doubleClick(test.getCoordinates());
    assertEquals(2, getMonitor().downs());
    assertEquals(2, getMonitor().ups());
    assertEquals(2, getMonitor().leftClicks());
    assertEquals(1, getMonitor().doubleClicks());
  }

  @Test
  public void tripleClick() {
    getMouse().tripleClick(test.getCoordinates());
    assertEquals(3, getMonitor().downs());
    assertEquals(3, getMonitor().ups());
  }

  @Test
  public void tripleClickMarksSentence() {
    getMouse().tripleClick(sentence.getCoordinates());
    assertEquals(SENTENCE_TEXT, sentence.getText());
  }

  @Test
  public void quadrupleClick() {
    getMouse().quadrupleClick(test.getCoordinates());
    assertEquals(4, getMonitor().downs());
    assertEquals(4, getMonitor().ups());
  }

  @Test
  public void quadrupleClickMarksParagraph() {
    getMouse().quadrupleClick(paragraph.getCoordinates());
    assertEquals(PARAGRAPH_TEXT, paragraph.getText());
  }

  @Test
  public void contextClick() {
    getMouse().contextClick(test.getCoordinates());
    assertEquals(1, getMonitor().rightClicks());
  }

  private OperaMouse getMouse() {
    return (OperaMouse) driver.getMouse();
  }

  private MouseMonitor getMonitor() {
    return new MouseMonitor();
  }

  private class MouseMonitor {

    private final Gson gson = new Gson();

    private List<MouseEvent> events = Lists.newLinkedList();

    public MouseMonitor() {
      sync();
    }

    public int overs() {
      return Collections.frequency(events, new MouseEvent("mouseover"));
    }

    public int outs() {
      return Collections.frequency(events, new MouseEvent("mouseout"));
    }

    public int downs() {
      return Collections.frequency(events, new MouseEvent("mousedown"));
    }

    public int ups() {
      return Collections.frequency(events, new MouseEvent("mouseup"));
    }

    public int leftClicks() {
      return Collections.frequency(events, new MouseEvent("click", 0));
    }

    public int doubleClicks() {
      return Collections.frequency(events, new MouseEvent("dblclick"));
    }

    private int rightClicks() {
      int contextClicks = 0;

      for (int i = 0; i <= events.size(); i++) {
        MouseEvent event = events.get(i);
        MouseEvent nextEvent;

        if (events.size() == (i + 1)) {
          break;
        }

        nextEvent = events.get(i + 1);

        if (event.equals(new MouseEvent("mousedown", 2)) &&
            nextEvent.equals(new MouseEvent("mouseup", 2))) {
          contextClicks++;
        }
      }

      return contextClicks;
    }

    private void sync() {
      events = gson.fromJson(String.valueOf(driver.executeScript("return log")),
                             new TypeToken<Collection<MouseEvent>>() {
                             }.getType());
    }

    private class MouseEvent {

      private final String type;
      private final int button;
      private final List<String> modifiers;

      public MouseEvent(String type) {
        this(type, 0);
      }

      public MouseEvent(String type, int button) {
        this(type, button, new ArrayList<String>());
      }

      public MouseEvent(String type, int button, List<String> modifiers) {
        this.type = checkNotNull(type);
        this.button = checkNotNull(button);
        this.modifiers = checkNotNull(modifiers);
      }

      public String getType() {
        return type;
      }

      public int getButton() {
        return button;
      }

      public Iterable<String> getModifiers() {
        return modifiers;
      }

      @Override
      public String toString() {
        return String.format("%s [type=%s, button=%s, modifiers=%s]",
                             getClass().getSimpleName(),
                             type,
                             button,
                             modifiers);
      }

      @Override
      public boolean equals(Object o) {
        return o instanceof MouseEvent &&
               getType().equals(((MouseEvent) o).getType()) &&
               getButton() == ((MouseEvent) o).getButton() &&
               getModifiers().equals(((MouseEvent) o).getModifiers());
      }

    }

  }

}