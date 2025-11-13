# 알림 아이콘 이미지

이 폴더는 푸시 알림에서 사용할 타입별 아이콘 이미지를 저장합니다.

## 필요한 이미지 파일들

- `daily-record-icon.png` - 하루 기록 알림 아이콘 🏠
- `exercise-record-icon.png` - 운동 기록 알림 아이콘 💪  
- `habit-record-icon.png` - 습관 기록 알림 아이콘 ✨
- `goal-setting-icon.png` - 목표 설정 알림 아이콘 🎯
- `default-icon.png` - 기본 알림 아이콘

## 사용 방법

이미지 파일들을 추가하면 FCM 푸시 알림의 `data.imageUrl` 필드에 URL이 포함됩니다.

```json
{
  "data": {
    "mainType": "DAILY",
    "notificationType": "DAILY_RECORD_REMINDER", 
    "imageUrl": "/images/notifications/daily-record-icon.png"
  }
}
```

프론트엔드에서는 `imageUrl`이 있으면 커스텀 아이콘을 사용하고, 없으면 앱 로고를 사용하면 됩니다.