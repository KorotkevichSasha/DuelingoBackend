import { Grid, Card, CardContent, Typography, Button, Box } from '@mui/material';
import {
  People as PeopleIcon,
  School as SchoolIcon,
  Assessment as AssessmentIcon,
  EmojiEvents as EmojiEventsIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  fetchUserStatistics,
  fetchContentStatistics,
  fetchLearningStatistics,
} from '../api/statistics';

const StatCard = ({
  title,
  value,
  icon: Icon,
  color,
}: {
  title: string;
  value: number | string;
  icon: React.ElementType;
  color: string;
}) => (
  <Card>
    <CardContent>
      <Box display="flex" alignItems="center" mb={2}>
        <Icon sx={{ color, fontSize: 40, mr: 1 }} />
        <Typography variant="h6" color="textSecondary">
          {title}
        </Typography>
      </Box>
      <Typography variant="h4">{value}</Typography>
    </CardContent>
  </Card>
);

export default function Dashboard() {
  const navigate = useNavigate();
  const { data: userStats } = useQuery({
    queryKey: ['userStatistics'],
    queryFn: fetchUserStatistics,
  });

  const { data: contentStats } = useQuery({
    queryKey: ['contentStatistics'],
    queryFn: fetchContentStatistics,
  });

  const { data: learningStats } = useQuery({
    queryKey: ['learningStatistics'],
    queryFn: fetchLearningStatistics,
  });

  const quickActions = [
    {
      title: 'Manage Users',
      description: 'View and manage user accounts',
      action: () => navigate('/users'),
    },
    {
      title: 'Content Management',
      description: 'Manage tests and questions',
      action: () => navigate('/content'),
    },
    {
      title: 'View Statistics',
      description: 'Analyze platform performance',
      action: () => navigate('/statistics'),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={3}>
          <StatCard
            title="Total Users"
            value={userStats?.totalUsers || 0}
            icon={PeopleIcon}
            color="#2196f3"
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <StatCard
            title="Total Tests"
            value={contentStats?.totalTests || 0}
            icon={SchoolIcon}
            color="#4caf50"
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <StatCard
            title="Tests Completed"
            value={learningStats?.totalTestsCompleted || 0}
            icon={AssessmentIcon}
            color="#ff9800"
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <StatCard
            title="Total Duels"
            value={learningStats?.totalDuelsPlayed || 0}
            icon={EmojiEventsIcon}
            color="#f50057"
          />
        </Grid>
      </Grid>

      <Typography variant="h5" gutterBottom>
        Quick Actions
      </Typography>
      <Grid container spacing={3}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} md={4} key={index}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {action.title}
                </Typography>
                <Typography color="textSecondary" paragraph>
                  {action.description}
                </Typography>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={action.action}
                >
                  Go to {action.title}
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
} 