package com.opera.core.systems.scope.handlers;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriverException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.beans.Action;
import com.opera.core.systems.scope.internal.OperaMouseKeys;

/**
 * This library handles actions for Core 2.5+
 * Core 2.5 uses a different (binary) protocol
 * and different serializers
 * @author Deniz Turkoglu
 *
 */
public class PbActionHandler extends ScopeActions {

    public PbActionHandler(ScopeServices services) {
            super(services);
    }

    @Override
    public void click(RenderedWebElement element, String locator) {
            Point point = element.getLocation();
            services.getExec().mouseAction(point.x, point.y, OperaMouseKeys.LEFT);
    }

    @Override
    public void click(RenderedWebElement element, int x, int y) {
            Point point = element.getLocation();
            services.getExec().mouseAction(point.x + x, point.y + y, OperaMouseKeys.LEFT);
    }

    @Override
    public Action createAction(String using) {
            throw new NotImplementedException();
    }

    @Override
    public Action createAction(String using, String param) {
            throw new NotImplementedException();
    }

    @Override
    public void get(String url) {
            services.getExec().action("Go", url);
    }

    @Override
    public void rightClick(RenderedWebElement element) {
            Point point = element.getLocation();
            services.getExec().mouseAction(point.x, point.y, OperaMouseKeys.RIGHT);
    }

    @Override
    public void saveScreenshot(File pngFile) {
        if (pngFile == null) {
            throw new IllegalArgumentException("Method parameter pngFile must not be null");
        }

        File dir = pngFile.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new WebDriverException("Could not create directory " + dir.getAbsolutePath());
        }

        Canvas canvas = new Canvas();
        canvas.setX(0);
        canvas.setY(0);

        String[] dimensions = scriptDebugger.executeJavascript("return (window.innerWidth + \",\" + window.innerHeight);").split(",");
        canvas.setH(Integer.valueOf(dimensions[1]));
        canvas.setW(Integer.valueOf(dimensions[0]));
        canvas.setViewPortRelative(true);

        ScreenShotReply reply = services.getExec().screenWatcher(canvas, 1l, true);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(pngFile.getAbsolutePath());
            stream.write(reply.getPng());
            stream.close();
        } catch (FileNotFoundException e) {
            //ignore
        } catch (IOException e) {
            //TODO log
        }
    }
}
