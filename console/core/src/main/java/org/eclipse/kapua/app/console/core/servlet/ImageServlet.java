/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.core.servlet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.io.FileCleaningTracker;
import org.eclipse.kapua.app.console.module.api.setting.ConsoleSetting;
import org.eclipse.kapua.app.console.module.api.setting.ConsoleSettingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = -5016170117606322129L;
    private static final Logger logger = LoggerFactory.getLogger(ImageServlet.class);

    DiskFileItemFactory diskFileItemFactory;
    FileCleaningTracker fileCleaningTracker;

    private static final int QR_CODE_SIZE = 134;

    @Override
    public void destroy() {
        super.destroy();

        logger.info("Servlet {} destroyed", getServletName());

        if (fileCleaningTracker != null) {
            logger.info("Number of temporary files tracked: " + fileCleaningTracker.getTrackCount());
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();

        logger.info("Servlet {} initialized", getServletName());

        ServletContext ctx = getServletContext();
        fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(ctx);

        int sizeThreshold = ConsoleSetting.getInstance().getInt(ConsoleSettingKeys.FILE_UPLOAD_INMEMORY_SIZE_THRESHOLD);
        File repository = new File(System.getProperty("java.io.tmpdir"));

        logger.info("DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD: {}", DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
        logger.info("DiskFileItemFactory: using size threshold of: {}", sizeThreshold);

        diskFileItemFactory = new DiskFileItemFactory(sizeThreshold, repository);
        diskFileItemFactory.setFileCleaningTracker(fileCleaningTracker);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("image/png");

        String reqPathInfo = req.getPathInfo();
        if (reqPathInfo == null) {
            resp.sendError(404);
            return;
        }

        logger.debug("req.getRequestURI(): {}", req.getRequestURI());
        logger.debug("req.getRequestURL(): {}", req.getRequestURL());
        logger.debug("req.getPathInfo(): {}", req.getPathInfo());

        if (reqPathInfo.equals("/2FAQRcode")) {
            doGetQRCode(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    private void doGetQRCode(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String username = req.getParameter("username");
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("username");
            }

            String accountName = req.getParameter("accountName");
            if (accountName == null || accountName.isEmpty()) {
                throw new IllegalArgumentException("accountName");
            }

            String key = req.getParameter("key");
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("key");
            }

            // this is only used to avoid that images are taken by the browser cache instead of being generated again
            String timestamp = req.getParameter("timestamp");
            if (timestamp == null || timestamp.isEmpty()) {
                throw new IllegalArgumentException("timestamp");
            }
            // TODO: check if the user has right to view his profile

            // url to qr_barcode encoding
            StringBuilder sb = new StringBuilder();
            sb.append("otpauth://totp/")
                    .append(username)
                    .append("@")
                    .append(accountName) // TODO: not sure that we also need the account name
                    .append("?secret=")
                    .append(key);

            BitMatrix bitMatrix = new QRCodeWriter().encode(sb.toString(),
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE);

            BufferedImage resultImage = buildImage(bitMatrix);
            ImageIO.write(resultImage, "png", resp.getOutputStream());

        } catch (IllegalArgumentException iae) {
            resp.sendError(400, "Illegal value for query parameter: " + iae.getMessage());
        } catch (Exception e) {
            logger.error("Exception generating two-factor authentication key", e);
            throw new ServletException("Exception generating two-factor authentication key");
        }
    }

    private BufferedImage buildImage(BitMatrix bitMatrix) {
        BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        BufferedImage resultImage = new BufferedImage(QR_CODE_SIZE,
                QR_CODE_SIZE,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = resultImage.getGraphics();
        g.drawImage(qrCodeImage, 0, 0, new Color(232, 232, 232, 255), null);

        return resultImage;
    }
}
