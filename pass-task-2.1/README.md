# Travel Companion App

A simple Android app for the Mobile Application Development task. It converts:

- Currency
- Fuel/travel values
- Temperature

## Features

- Category selector for Currency, Fuel & Travel, and Temperature
- Source and destination dropdowns
- Numeric input field
- Convert button
- Result output card
- Basic validation to prevent crashes from blank or non-numeric input
- Identity conversion handling
- Negative value blocking for fuel/travel conversions

## Conversion rules used

### Currency (fixed task rates)
- 1 USD = 1.55 AUD
- 1 USD = 0.92 EUR
- 1 USD = 148.50 JPY
- 1 USD = 0.78 GBP

### Fuel & Travel
- 1 MPG = 0.425 km/L
- 1 Gallon (US) = 3.785 Liters
- 1 Nautical Mile = 1.852 Kilometers

### Temperature
- Fahrenheit = (Celsius × 1.8) + 32
- Celsius = (Fahrenheit − 32) / 1.8
- Kelvin = Celsius + 273.15

## How to run in Android Studio

1. Open Android Studio.
2. Choose **Open** and select the `travel-companion-app` folder.
3. Allow Gradle to sync.
4. Start an emulator from **Device Manager**.
5. Press **Run**.

## Recommended emulator

- Device: Pixel 7 or Pixel 8
- API level: 34
- Android 14

## Package name

`com.example.travelcompanion`
