import { Container, Link, Paper, Stack, Typography } from '@mui/material';

const supportEmail = import.meta.env.VITE_SUPPORT_EMAIL as string | undefined;
const configuredOperator = (import.meta.env.VITE_OPERATOR_NAME as string | undefined) || 'DuelRush';
const operatorName = configuredOperator.replace(/\s*\(staging\)\s*/gi, '').trim() || 'DuelRush';

export default function Privacy() {
  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Paper elevation={2} sx={{ p: { xs: 3, md: 5 }, borderRadius: 4 }}>
        <Stack spacing={2.5}>
          <Typography variant="h3" component="h1">Политика конфиденциальности DuelRush</Typography>
          <Typography color="text.secondary">Оператор: {operatorName}. Последнее обновление: 9 сентября 2026 г.</Typography>
          <Typography variant="h5">Какие данные обрабатываются</Typography>
          <Typography>Имя пользователя, электронная почта, профильное изображение, учебный прогресс, словарь, дружеские связи, результаты дуэлей, достижения и технические журналы безопасности.</Typography>
          <Typography variant="h5">Микрофон и речь</Typography>
          <Typography>Микрофон используется только после явного действия пользователя в упражнениях на произношение. Распознавание выполняет системный сервис речи, выбранный на устройстве (например, сервис Google), и обработка аудио может происходить по правилам этого поставщика. DuelRush не отправляет и не хранит запись голоса на своих серверах; приложение получает только распознанный текст.</Typography>
          <Typography variant="h5">Для чего используются данные</Typography>
          <Typography>Для авторизации, синхронизации прогресса, работы учебных упражнений и дуэлей, защиты сервиса, поддержки пользователей и обработки жалоб.</Typography>
          <Typography variant="h5">Реклама</Typography>
          <Typography>В магазине приложения может показываться вознаграждаемая реклама через Yandex Mobile Ads SDK. Перед запуском рекламного SDK приложение предлагает разрешить или отклонить обработку технических данных для показа и измерения рекламы. В зависимости от настроек и применимого законодательства Яндекс может обрабатывать IP-адрес, параметры устройства и операционной системы, тип интернет-соединения, сведения о показах и взаимодействии с рекламой и рекламные идентификаторы. DuelRush удаляет разрешение Android на рекламный идентификатор из текущей версии приложения и не передаёт Яндексу имя, адрес электронной почты, учебные ответы или данные аккаунта. Подробнее: <Link href="https://yandex.com/legal/international_ads_privacy_policy/en/" target="_blank" rel="noreferrer">политика конфиденциальности рекламы Яндекса</Link>.</Typography>
          <Typography variant="h5">Хранение, передача и защита</Typography>
          <Typography>Данные передаются по защищённому HTTPS-соединению и хранятся, пока существует аккаунт или пока это необходимо для работы сервиса и предотвращения злоупотреблений. Для размещения API и базы данных используются Render и MongoDB Atlas. Электронная почта для подтверждения аккаунта отправляется через Gmail. DuelRush не продаёт персональные данные.</Typography>
          <Typography variant="h5">Удаление и права пользователя</Typography>
          <Typography>Аккаунт и связанные профильные, учебные и социальные данные можно удалить в настройках приложения. Запрос также можно отправить на отдельной странице удаления аккаунта. Ограниченная информация может сохраняться дольше только когда это требуется законом, для безопасности или рассмотрения жалоб.</Typography>
          <Typography variant="h5">Контакт</Typography>
          <Typography>{supportEmail ? <Link href={`mailto:${supportEmail}`}>{supportEmail}</Link> : 'Контакт поддержки будет указан до публикации приложения.'}</Typography>
          <Link href="/delete-account">Перейти к удалению аккаунта</Link>
        </Stack>
      </Paper>
    </Container>
  );
}
