import { Button, Container, Paper, Stack, Typography } from '@mui/material';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

const supportEmail = import.meta.env.VITE_SUPPORT_EMAIL as string | undefined;

export default function DeleteAccount() {
  const subject = encodeURIComponent('DuelRush — запрос на удаление аккаунта');
  const body = encodeURIComponent('Укажите имя пользователя и адрес электронной почты аккаунта. Не отправляйте пароль.');
  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <Paper elevation={2} sx={{ p: { xs: 3, md: 5 }, borderRadius: 4 }}>
        <Stack spacing={2.5} alignItems="flex-start">
          <DeleteForeverIcon color="error" sx={{ fontSize: 52 }} />
          <Typography variant="h3" component="h1">Удаление аккаунта DuelRush</Typography>
          <Typography>В приложении: Профиль → Настройки → Удалить аккаунт. Это удаляет аккаунт и связанные данные.</Typography>
          <Typography>Если приложение недоступно, отправьте запрос поддержке. Укажите только имя пользователя и почту — пароль никогда не требуется.</Typography>
          <Button variant="contained" color="error" size="large" disabled={!supportEmail}
            href={supportEmail ? `mailto:${supportEmail}?subject=${subject}&body=${body}` : undefined}>
            Запросить удаление
          </Button>
          {!supportEmail && <Typography color="error">Перед публикацией необходимо настроить VITE_SUPPORT_EMAIL.</Typography>}
          <Typography variant="body2" color="text.secondary">После проверки владения аккаунтом запрос будет выполнен в разумный срок. О необходимых сроках хранения данных пользователь будет уведомлён отдельно.</Typography>
        </Stack>
      </Paper>
    </Container>
  );
}
