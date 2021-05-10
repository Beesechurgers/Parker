#
# Copyright (c) 2021, Shashank Verma <shashank.verma2002@gmail.com>(shank03)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#

import re
import string
import sys
import qrcode
import cv2
import imutils
import numpy as np
import pytesseract


class Detector:
    def __init__(self):
        self.regex = '^[A-Z]{2}[][0-9]{1,2}(?:[A-Z])?(?:[A-Z]*)?[0-9]{4}$'
        self.camera = None
        self.detected = None

    def __del__(self):
        if self.camera is not None:
            self.camera.release()

    def __format_text(self, text):
        filtered = [x for x in list(text) if x in string.ascii_letters + string.digits]
        literal = ''.join(filtered).upper().strip()
        if len(literal) == 0:
            return 'None'
        else:
            return literal

    def detect_license_number(self):
        if self.camera is None:
            return "None"
        if not self.camera.isOpened():
            return "None"

        while True:
            ret, frame = self.camera.read()

            img = frame
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            gray = cv2.bilateralFilter(gray, 13, 15, 15)

            edged = cv2.Canny(gray, 30, 200)
            contours = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            contours = imutils.grab_contours(contours)
            contours = sorted(contours, key=cv2.contourArea, reverse=True)[:10]
            screen_cnt = None

            for c in contours:
                peri = cv2.arcLength(c, True)
                approx = cv2.approxPolyDP(c, 0.018 * peri, True)

                if len(approx) == 4:
                    screen_cnt = approx
                    break

            if screen_cnt is not None:
                cv2.drawContours(img, [screen_cnt], -1, (0, 0, 255), 3)

                mask = np.zeros(gray.shape, np.uint8)
                cv2.drawContours(mask, [screen_cnt], 0, 255, -1)
                cv2.bitwise_and(img, img, mask=mask)

                (x, y) = np.where(mask == 255)
                (top_x, top_y) = (np.min(x), np.min(y))
                (bottom_x, bottom_y) = (np.max(x), np.max(y))

                cropped = gray[top_x:bottom_x + 1, top_y:bottom_y + 1]
                text = pytesseract.image_to_string(cropped, lang='eng')
                text = self.__format_text(text)
                # print(f"Possible: {text}")
                if re.match(self.regex, text):
                    # print(f"Detected: {text}")
                    self.detected = text
                    self.stop_camera()
                    return text

            cv2.imshow('Capture', frame)
            if cv2.waitKey(10) == ord('q'):
                return "None"

    def start_camera(self):
        if self.camera is None:
            self.camera = cv2.VideoCapture(0)

    def stop_camera(self):
        if self.camera is not None:
            self.camera.release()
            self.camera = None

    def get_detected(self):
        if self.detected is None:
            return "None"
        else:
            return self.detected


detector = Detector()
detector.start_camera()
license_number = detector.detect_license_number()

f = open("license_number.txt", "w")
f.write(license_number)
f.close()

if sys.argv[1] == '1':
    qr = qrcode.QRCode(version=1, error_correction=qrcode.constants.ERROR_CORRECT_H, box_size=25, border=2)
    qr.add_data(license_number)
    qr.make(fit=True)

    qr.make_image(fill_color="black", back_color="white").save("qrcode.jpg")
exit(0)
