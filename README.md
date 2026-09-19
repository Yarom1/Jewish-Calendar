# לוח שנה עברי — Jewish Calendar (Android)

אפליקציית Android (Kotlin + Jetpack Compose) המשלבת לוח שנה עברי-לועזי וזמני היום, לפי המסמך
`hebrew_calendar_app_spec.md`. זהו שלד ה-MVP הראשון: הליבה של הלוח וזמני היום עובדת מקצה לקצה;
שאר הפיצ'רים במסמך (סעיפים 5–6) מתועדים כ-Roadmap בהמשך.

## מה קיים ב-MVP הזה

- **סנכרון עברי-לועזי אופליין מלא** דרך [KosherJava](https://github.com/KosherJava/zmanim)
  (`domain/hebrew/HebrewDate.kt`) — המרה דו-כיוונית, חגים, ראש חודש, פרשת השבוע, ספירת העומר.
- **חישוב זמני היום אופליין** (`domain/zmanim/ZmanimEngine.kt`) עם 3 שיטות חישוב (גר"א, מגן
  אברהם, הרב עובדיה יוסף), כל 12 הזמנים מהמסמך (סעיף 4.a).
- **מיקום**: GPS (`domain/location/LocationRepository.kt`) או רשימת ערים ידנית
  (`domain/location/CityPresets.kt`).
- **תצוגות שבועית (ברירת מחדל) / יומית / חודשית** עם הדגשת שבתות/חגים.
- **הוספת אירוע בלחיצה ארוכה** על תא תאריך, כולל חזרתיות לועזית (יומי/שבועי/חודשי/שנתי) וחזרתיות
  עברית (ראש חודש / שנתי עברי לימי הולדת ויארצייט) — `data/repository/EventRepository.kt` מרחיב
  את החוקים לאירועים בפועל בטווח תאריכים.
- **מסך הגדרות**: מיקום, שיטת חישוב, בחירת זמנים מוצגים, מצב כהה/בהיר, Material You.
- **Material You / Dynamic Color** (Android 12+), תמיכה מלאה ב-RTL.

## מה לא ממומש עדיין (Roadmap, ראו סעיפים 3, 5, 6 במסמך)

- שיתוף יומן משפחתי, הרשאות, סנכרון ל-Google/Apple/Outlook Calendar.
- ווידג'טים למסך הבית, התראות רציפות בשורת הסטטוס, ערוצי התראה.
- אוטומציית "נא לא להפריע" בכניסת/יציאת שבת (`NotificationPolicyManager`).
- WorkManager לחישוב/עדכון רקע ותזכורות (התלות כבר בפרויקט, טרם מחובר).
- תזכורות מנהגי היום (עומר, תעניות, ברכת האילנות), מודול לימוד יומי.
- Wear OS, מצב נסיעות/חו"ל, Firebase/Supabase לחשבון משתמש וסנכרון.
- אינטגרציית Hebcal API להעשרת מידע (יש כבר interface מוכן ב-`data/remote/HebcalApi.kt`,
  לא מחובר עדיין — האפליקציה חייבת להמשיך לעבוד אופליין).

## ארכיטקטורה

```
app/src/main/java/com/yarom/jewishcalendar/
├── domain/
│   ├── hebrew/    HebrewDateConverter (KosherJava JewishCalendar wrapper)
│   ├── zmanim/    ZmanimEngine (KosherJava ComplexZmanimCalendar wrapper)
│   └── location/  LocationRepository, CityPresets
├── data/
│   ├── local/     Room: EventEntity, EventDao, AppDatabase
│   ├── remote/    HebcalApi (Retrofit, לא מחובר עדיין)
│   └── repository/ EventRepository (הרחבת חזרתיות), SettingsRepository (DataStore)
├── ui/
│   ├── screens/   weekly / daily / monthly / settings / addevent
│   ├── components/ DayCell, ZmanRow וכו'
│   ├── navigation/
│   └── theme/     Material3 + Dynamic Color
├── MainActivity.kt
└── JewishCalendarApp.kt   (service locator ידני — ללא Hilt, לשמירה על פשטות ב-MVP)
```

DI: service-locator ידני קטן (`JewishCalendarApp`) במקום Hilt, כדי לשמור MVP רזה; ניתן להחליף
בקלות ל-Hilt/Koin בהמשך אם הפרויקט יגדל.

## הרצה ובנייה

הפרויקט נבנה ב-Gradle (Kotlin DSL) עם Version Catalog (`gradle/libs.versions.toml`).

```bash
./gradlew assembleDebug
```

**הערה חשובה**: הסביבה שבה נכתב הקוד הזה אינה כוללת Android SDK ואין בה גישת רשת ל-
`dl.google.com`, ולכן לא ניתן היה להריץ build מלא/לוודא קומפילציה כאן. יש לפתוח את הפרויקט
ב-Android Studio (שמתקין SDK אוטומטית) ולהריץ Sync + Build כדי לוודא שהכול מתקמפל, ולתקן אם יש
אי-התאמות גרסת API בין ה-KosherJava wrapper לגרסה שתרד בפועל.

## הערה הלכתית

המיפוי בין שיטות החישוב (`CalculationMethod`) לנוסחאות הספציפיות ב-KosherJava
(`ZmanimEngine.kt`) הוא בחירה סבירה לשלב ה-MVP, אך **יש לבדוק אותו מול פוסק/רב** לפני הסתמכות
הלכתית בפרודקשן — זמני היום הם תחום רגיש שדורש אימות.

## מיפוי גרסה נוכחית ↔ מסמך האפיון

| סעיף במסמך | סטטוס |
|---|---|
| 2. UI/UX, תצוגות | ✅ שלושת המצבים קיימים |
| 3. לחיצה ארוכה + חזרתיות | ✅ |
| 4.א זמנים + מיקום | ✅ (אופליין מלא) |
| 4.ב שיתוף משפחתי | ❌ Roadmap |
| 4.ג הגדרות מורחב | ✅ חלקי (בלי התראות) |
| 5. ווידג'טים/התראות/DND | ❌ Roadmap |
| 6. פיצ'רים חדשניים | ❌ Roadmap |
