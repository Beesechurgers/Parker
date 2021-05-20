# Parker

#### A parking automizing system, where all that user need to do is scan QR code on entering, and exit without doing anything <br> <br>

## How it works
- **Setup:** <br>
**1.** C++ compiled command-line program. <br>
**2.** Firebase database. <br>
**3.** Camera at entry and exit barricades. <br>
**4.** Android app for user interaction. <br><br>

- **Parking Entry:** <br>
When driver enters parking area, the car will stop at entry barricade. <br>
The camera on barricade will scan and detect the number plate and generate QR code containing the detected car number and session ID. The QR code will be shown on the panel attached to the side of barricade something like this: (only QR code) <br>
<img src="images/qrscan.png" width="626px" height="415px"> <br>
Once driver scans this QR code from app, the session will start and barricade will open. Driver can now park the car. <br> <br>

- **Parking Exit:** <br>
The car will again stop at exit barricade and the camera on it will scan and detect the number plate. If the number plate is the same that entered previously, then terminate the session, notify driver with the calculated amount and barricade will open for car to exit. <br><br>

## Screenshots of App
<details>
<summary>Click to expand</summary>

<img src="images/parker_launcher.png" width="360" height="640"> &nbsp;
<img src="images/parker_car_number.png" width="360" height="640"> &nbsp;
<img src="images/parker_plain_main.png" width="360" height="640"> &nbsp; <br>

<img src="images/parker_scan_qrcode.png" width="360" height="640"> &nbsp;
<img src="images/parker_session_started.png" width="360" height="640"> &nbsp;
<img src="images/parker_ongoin_session.png" width="360" height="640"> &nbsp; <br>

<img src="images/parker_session_completed.png" width="360" height="640"> &nbsp;
<img src="images/parker_pending_payment.png" width="360" height="640"> &nbsp;
<img src="images/parker_pay_now.png" width="360" height="640"> &nbsp; <br>

<img src="images/parker_payment_history.png" width="360" height="640"> &nbsp;
</details> <br><br>

## Tool, Frameworks and Languages used
| Product | Tool | Frameworks | Languages |
|:-------:|:-----:|:----------:|:---------:|
| App     | Android Studio | AndroidX<br>Firebase<br>Material UI<br>CameraX | Kotlin <br>Java <br>XML |
| Command Line<br>Backend | CLion | Firebase<br>Node js<br>OpenCV<br>Tesseract-OCR | C++<br>Python<br>Javascript |
<br><br>

## Developers
- Shashank Verma
- Naman Agrawal
- Shashank Saxena
- Jay Aggarwal
