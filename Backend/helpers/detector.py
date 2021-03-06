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

import json
import os
import re
import signal
import string
import sys

import cv2
import imutils
import numpy as np
import pytesseract
import qrcode
import requests


def handler(signum, frame):
    raise Exception("Timeout")


print("Detecting ...")
regex = '^[A-Z]{2}[][0-9]{1,2}(?:[A-Z])?(?:[A-Z]*)?[0-9]{4}$'
detected = None
std_img = None
camera = cv2.VideoCapture(0)

signal.signal(signal.SIGALRM, handler)
signal.alarm(5)


def format_text(text):
    filtered = [x for x in list(text) if x in string.ascii_letters + string.digits]
    literal = ''.join(filtered).upper().strip().replace(" ", "")
    if len(literal) == 0:
        return 'None'
    else:
        return literal


try:
    while True:
        ret, frame = camera.read()

        img = frame
        std_img = frame.copy()
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
            text = format_text(text)
            # print(f"Possible: {text}")
            if re.match(regex, text):
                # print(f"Detected: {text}")
                detected = text
                camera.release()
                cv2.imwrite("car.jpg", std_img)
                break

        cv2.imshow('Capture', frame)
        if cv2.waitKey(10) == ord('q'):
            break
finally:
    signal.alarm(0)
    cv2.destroyAllWindows()
    if not os.path.exists("car.jpg"):
        cv2.imwrite("car.jpg", std_img)

    session_id = str(sys.argv[3])

    with open("car.jpg", 'rb') as fp:
        response = requests.post('https://api.platerecognizer.com/v1/plate-reader/',
                                 data=dict(regions=['in'], config=json.dumps(dict(region="plate"))),
                                 files=dict(upload=fp),
                                 headers={'Authorization': 'Token ' + sys.argv[1]})
    license_number = str(response.json()["results"][0]["plate"]).upper().strip().replace(" ", "")
    os.remove("car.jpg")
    if re.match(regex, license_number):
        f = open("license_number.txt", "w")
        f.write(license_number)
        f.close()

        s = open("session.txt", "w")
        s.write(session_id)
        s.close()
    else:
        print("Invalid car number")
        exit(1)

    if sys.argv[2] == '1':
        qr = qrcode.QRCode(version=1, error_correction=qrcode.constants.ERROR_CORRECT_H, box_size=15, border=2)
        qr.add_data(session_id + "/" + license_number)
        qr.make(fit=True)

        qr.make_image(fill_color="black", back_color="white").save("qrcode.png")
    exit(0)
