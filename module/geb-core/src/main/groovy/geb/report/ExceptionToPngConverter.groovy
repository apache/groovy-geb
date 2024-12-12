/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb.report

import javax.imageio.ImageIO
import java.awt.*
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.util.List

import static java.awt.Color.BLACK
import static java.awt.Color.WHITE
import static java.awt.Font.PLAIN
import static java.awt.image.BufferedImage.TYPE_INT_RGB

class ExceptionToPngConverter {
    private static final String FONT_TYPE = 'Monospaced'
    private static final int FONT_SIZE = 12
    final static int LINE_SPACING = 2
    Throwable throwable

    ExceptionToPngConverter(Throwable t) {
        throwable = t
    }

    byte[] convert(String headline) {
        convertLines([headline] + throwableLines(throwable))
    }

    private List<String> throwableLines(Throwable t) {
        def lines = []
        lines << t.toString()
        lines += t.stackTrace.collect {
            "at $it"
        }
        if (t.cause) {
            lines << 'Caused by:'
            lines += throwableLines(t.cause)
        }
        lines
    }

    private byte[] convertLines(List<String> lines) {
        def fontRenderContext = new BufferedImage(1, 1, TYPE_INT_RGB).createGraphics().fontRenderContext
        def font = new Font(FONT_TYPE, PLAIN, FONT_SIZE)
        def image = createImage(lines, font, fontRenderContext)
        drawLines(lines, image, font, fontRenderContext)
        convertToPngBytes(image)
    }

    private byte[] convertToPngBytes(BufferedImage image) {
        def stream = new ByteArrayOutputStream()
        ImageIO.write(image, 'png', stream)
        stream.toByteArray()
    }

    private BufferedImage createImage(List<String> lines, Font font, FontRenderContext fontRenderContext) {
        def w = lines.collect { font.getStringBounds(it, fontRenderContext).width }.max() as int
        def h = lines.collect { (font.getStringBounds(it, fontRenderContext).height as int) + LINE_SPACING }.sum()

        new BufferedImage(w, h, TYPE_INT_RGB)
    }

    private byte[] drawLines(List<String> lines, BufferedImage image, Font font, FontRenderContext fontRenderContext) {
        def g = image.createGraphics()

        g.color = WHITE
        g.fillRect(0, 0, image.width, image.height)
        g.color = BLACK
        g.font = font

        lines.inject(0) { totalHeight, line ->
            def bounds = font.getStringBounds(line, fontRenderContext)
            g.drawString(line, bounds.x as int, (totalHeight - bounds.y) as int)
            totalHeight + (bounds.height as int) + LINE_SPACING
        }

        g.dispose()
    }
}
