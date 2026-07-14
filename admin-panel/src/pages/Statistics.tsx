import { Grid, Card, CardContent, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import {
  fetchUserStatistics,
  fetchContentStatistics,
  fetchLearningStatistics,
} from '../api/statistics';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

export default function Statistics() {
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

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Platform Statistics
      </Typography>

      {/* User Statistics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Users
              </Typography>
              <Typography variant="h4">{userStats?.totalUsers || 0}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Users (24h)
              </Typography>
              <Typography variant="h4">
                {userStats?.activeUsersLast24h || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Words Learned
              </Typography>
              <Typography variant="h4">
                {learningStats?.totalWordsLearned || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Tests Completed
              </Typography>
              <Typography variant="h4">
                {learningStats?.totalTestsCompleted || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                User Registration Trend
              </Typography>
              <LineChart
                width={500}
                height={300}
                data={userStats?.registrationStats || []}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="count"
                  stroke="#8884d8"
                  name="New Users"
                />
              </LineChart>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Test Completion Statistics
              </Typography>
              <BarChart
                width={500}
                height={300}
                data={learningStats?.testCompletionStats || []}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="completions" fill="#82ca9d" name="Completions" />
                <Bar
                  dataKey="averageScore"
                  fill="#8884d8"
                  name="Average Score"
                />
              </BarChart>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Questions by Type
              </Typography>
              <PieChart width={500} height={300}>
                <Pie
                  data={
                    contentStats?.questionsByType
                      ? Object.entries(contentStats.questionsByType).map(
                          ([name, value]) => ({
                            name,
                            value,
                          })
                        )
                      : []
                  }
                  cx={250}
                  cy={150}
                  labelLine={false}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, percent }) =>
                    `${name} ${(percent * 100).toFixed(0)}%`
                  }
                >
                  {contentStats?.questionsByType &&
                    Object.keys(contentStats.questionsByType).map((_, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={COLORS[index % COLORS.length]}
                      />
                    ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Top Users by Progress
              </Typography>
              <BarChart
                width={500}
                height={300}
                data={learningStats?.topUsersByProgress || []}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="username" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="wordsLearned" fill="#8884d8" name="Words Learned" />
                <Bar
                  dataKey="testsCompleted"
                  fill="#82ca9d"
                  name="Tests Completed"
                />
                <Bar dataKey="duelsWon" fill="#ffc658" name="Duels Won" />
              </BarChart>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </div>
  );
} 