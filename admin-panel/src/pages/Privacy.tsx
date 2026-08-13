import { Container, Link, Paper, Stack, Typography } from '@mui/material';

const supportEmail = import.meta.env.VITE_SUPPORT_EMAIL as string | undefined;
const operatorName = (import.meta.env.VITE_OPERATOR_NAME as string | undefined) || 'DuelRush';

export default function Privacy() {
  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Paper elevation={2} sx={{ p: { xs: 3, md: 5 }, borderRadius: 4 }}>
        <Stack spacing={2.5}>
          <Typography variant="h3" component="h1">Политика конфиденциальности DuelRush</Typography>
          <Typography color="text.secondary">Оператор: {operatorName}. Последнее обновление: 7 августа 2026 г.</Typography>
          <Typography variant="h5">Какие данные обрабатываются</Typography>
          <Typography>Имя пользователя, электронная почта, профильное изображение, учебный прогресс, словарь, дружеские связи, результаты дуэлей, достижения и технические журналы безопасности.</Typography>
          <Typography variant="h5">Микрофон и речь</Typography>
          <Typography>Микрофон используется только после явного действия пользователя в упражнениях на произношение. Приложение запрашивает системное разрешение непосредственно перед использованием функции.</Typography>
          <Typography variant="h5">Для чего используются данные</Typography>
          <Typography>Для авторизации, синхронизации прогресса, работы учебных упражнений и дуэлей, защиты сервиса, поддержки пользователей и обработки жалоб.</Typography>
          <Typography variant="h5">Удаление и права пользователя</Typography>
          <Typography>Аккаунт и связанные данные можно удалить в настройках приложения. Запрос также можно отправить на отдельной странице удаления аккаунта.</Typography>
          <Typography variant="h5">Контакт</Typography>
          <Typography>{supportEmail ? <Link href={`mailto:${supportEmail}`}>{supportEmail}</Link> : 'Контакт поддержки будет указан до публикации приложения.'}</Typography>
          <Link href="/delete-account">Перейти к удалению аккаунта</Link>
        </Stack>
      </Paper>
    </Container>
  );
}
