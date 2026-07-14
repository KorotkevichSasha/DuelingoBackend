import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Tabs,
  Tab,
  Button,
  TextField,
  MenuItem,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  FormHelperText,
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef } from '@mui/x-data-grid';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { 
  fetchTests, 
  fetchQuestions, 
  createTest, 
  createQuestion,
} from '../api/content';
import type { 
  CreateTestRequest,
  CreateQuestionRequest 
} from '../api/content.ts';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`content-tabpanel-${index}`}
      aria-labelledby={`content-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const testColumns: GridColDef[] = [
  { field: 'id', headerName: 'ID', width: 90, minWidth: 90 },
  { field: 'topic', headerName: 'Topic', flex: 1.5, minWidth: 180 },
  { field: 'difficulty', headerName: 'Difficulty', flex: 1, minWidth: 120 },
  { field: 'questionCount', headerName: 'Questions', flex: 1, minWidth: 120 },
  { field: 'completionRate', headerName: 'Completion Rate', flex: 1, minWidth: 140, valueFormatter: (params) => `${params.value}%` },
  { field: 'averageScore', headerName: 'Average Score', flex: 1, minWidth: 140, valueFormatter: (params) => `${params.value}%` },
];

const questionColumns: GridColDef[] = [
  { field: 'id', headerName: 'ID', width: 90, minWidth: 90 },
  { field: 'topic', headerName: 'Topic', flex: 1.5, minWidth: 180 },
  { field: 'type', headerName: 'Type', flex: 1.2, minWidth: 150 },
  { field: 'difficulty', headerName: 'Difficulty', flex: 1, minWidth: 120 },
  { field: 'correctRate', headerName: 'Correct Rate', flex: 1, minWidth: 130, valueFormatter: (params) => `${params.value}%` },
];

export default function Content() {
  const [tabValue, setTabValue] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(25);
  const [topicFilter, setTopicFilter] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  // Dialog states
  const [testDialogOpen, setTestDialogOpen] = useState(false);
  const [questionDialogOpen, setQuestionDialogOpen] = useState(false);
  
  // Form states for creating a test
  const [newTest, setNewTest] = useState<CreateTestRequest>({
    topic: '',
    difficulty: 'EASY',
    description: '',
    questions: [],
  });

  // Form states for creating a question
  const [newQuestion, setNewQuestion] = useState<CreateQuestionRequest>({
    topic: '',
    type: 'FILL_IN_CHOICE',
    difficulty: 'EASY',
    content: '',
    options: ['', '', '', ''],
    correctAnswer: '',
  });
  
  // Form validation errors
  const [testErrors, setTestErrors] = useState<Record<string, string>>({});
  const [questionErrors, setQuestionErrors] = useState<Record<string, string>>({});

  const {
    data: testsData,
    isLoading: isTestsLoading,
    error: testsError
  } = useQuery({
    queryKey: ['tests', page, pageSize, topicFilter, difficultyFilter],
    queryFn: () => fetchTests(page, pageSize, topicFilter, difficultyFilter),
    enabled: tabValue === 0,
  });

  const {
    data: questionsData,
    isLoading: isQuestionsLoading,
    error: questionsError
  } = useQuery({
    queryKey: ['questions', page, pageSize, topicFilter, typeFilter, difficultyFilter],
    queryFn: () => fetchQuestions(page, pageSize, topicFilter, typeFilter, difficultyFilter),
    enabled: tabValue === 1,
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setPage(0);
    setTopicFilter('');
    setDifficultyFilter('');
    setTypeFilter('');
  };

  // Handle test creation dialog
  const handleTestDialogOpen = () => {
    setTestDialogOpen(true);
  };

  const handleTestDialogClose = () => {
    setTestDialogOpen(false);
    setNewTest({
      topic: '',
      difficulty: 'EASY',
      description: '',
      questions: [],
    });
    setTestErrors({});
  };

  // Handle question creation dialog
  const handleQuestionDialogOpen = () => {
    setQuestionDialogOpen(true);
  };

  const handleQuestionDialogClose = () => {
    setQuestionDialogOpen(false);
    setNewQuestion({
      topic: '',
      type: 'FILL_IN_CHOICE',
      difficulty: 'EASY',
      content: '',
      options: ['', '', '', ''],
      correctAnswer: '',
    });
    setQuestionErrors({});
  };

  // Handle test form changes
  const handleTestChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setNewTest(prev => ({ ...prev, [name]: value }));
    // Clear error when field is changed
    if (testErrors[name]) {
      setTestErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  // Handle test difficulty select
  const handleTestDifficultyChange = (e: any) => {
    setNewTest(prev => ({ ...prev, difficulty: e.target.value }));
  };

  // Handle question form changes
  const handleQuestionChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setNewQuestion(prev => ({ ...prev, [name]: value }));
    // Clear error when field is changed
    if (questionErrors[name]) {
      setQuestionErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  // Handle question options changes
  const handleOptionChange = (index: number, value: string) => {
    setNewQuestion(prev => {
      const newOptions = [...(prev.options || [])];
      newOptions[index] = value;
      return { ...prev, options: newOptions };
    });
  };

  // Handle question type and difficulty select
  const handleQuestionSelectChange = (e: any) => {
    const { name, value } = e.target;
    setNewQuestion(prev => ({ ...prev, [name]: value }));
  };

  // Submit new test
  const handleSubmitTest = async () => {
    // Validate form
    const errors: Record<string, string> = {};
    if (!newTest.topic) errors.topic = 'Topic is required';
    if (!newTest.difficulty) errors.difficulty = 'Difficulty is required';

    if (Object.keys(errors).length > 0) {
      setTestErrors(errors);
      return;
    }

    try {
      await createTest(newTest);
      enqueueSnackbar('Test created successfully', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['tests'] });
      handleTestDialogClose();
    } catch (error) {
      console.error('Error creating test:', error);
      enqueueSnackbar('Failed to create test', { variant: 'error' });
    }
  };

  // Submit new question
  const handleSubmitQuestion = async () => {
    // Validate form
    const errors: Record<string, string> = {};
    if (!newQuestion.topic) errors.topic = 'Topic is required';
    if (!newQuestion.type) errors.type = 'Type is required';
    if (!newQuestion.difficulty) errors.difficulty = 'Difficulty is required';
    if (!newQuestion.content) errors.content = 'Content is required';
    if (!newQuestion.correctAnswer) errors.correctAnswer = 'Correct answer is required';

    // Validate options for multiple choice questions
    if (newQuestion.type === 'FILL_IN_CHOICE' && newQuestion.options) {
      const emptyOptions = newQuestion.options.filter(o => !o).length;
      if (emptyOptions > 0) {
        errors.options = 'All options must be filled';
      }
    }

    if (Object.keys(errors).length > 0) {
      setQuestionErrors(errors);
      return;
    }

    try {
      await createQuestion(newQuestion);
      enqueueSnackbar('Question created successfully', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['questions'] });
      handleQuestionDialogClose();
    } catch (error) {
      console.error('Error creating question:', error);
      enqueueSnackbar('Failed to create question', { variant: 'error' });
    }
  };

  if (testsError || questionsError) {
    enqueueSnackbar('Error loading data', { variant: 'error' });
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Content Management
      </Typography>

      <Card>
        <CardContent>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab label="Tests" />
            <Tab label="Questions" />
          </Tabs>

          <TabPanel value={tabValue} index={0}>
            <Box sx={{ mb: 3 }}>
              <Button 
                variant="contained" 
                color="primary"
                onClick={handleTestDialogOpen}
              >
                Create New Test
              </Button>
            </Box>

            <Grid container spacing={3} sx={{ mb: 3 }}>
              <Grid item xs={12} md={4}>
                <TextField
                  select
                  fullWidth
                  label="Topic"
                  variant="outlined"
                  value={topicFilter}
                  onChange={(e) => setTopicFilter(e.target.value)}
                >
                  <MenuItem value="">All Topics</MenuItem>
                  <MenuItem value="grammar">Grammar</MenuItem>
                  <MenuItem value="vocabulary">Vocabulary</MenuItem>
                  <MenuItem value="pronunciation">Pronunciation</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  select
                  fullWidth
                  label="Difficulty"
                  variant="outlined"
                  value={difficultyFilter}
                  onChange={(e) => setDifficultyFilter(e.target.value)}
                >
                  <MenuItem value="">All Difficulties</MenuItem>
                  <MenuItem value="EASY">Easy</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HARD">Hard</MenuItem>
                </TextField>
              </Grid>
            </Grid>

            <DataGrid
              rows={testsData?.tests || []}
              columns={testColumns}
              rowCount={testsData?.totalTests || 0}
              loading={isTestsLoading}
              pageSizeOptions={[25, 50, 100]}
              paginationMode="server"
              paginationModel={{ page, pageSize }}
              onPaginationModelChange={(model) => {
                setPage(model.page);
                setPageSize(model.pageSize);
              }}
              disableRowSelectionOnClick
              sx={{
                height: 650,
                '& .MuiDataGrid-cell': {
                  fontSize: '0.95rem',
                },
                '& .MuiDataGrid-columnHeader': {
                  backgroundColor: '#f5f5f5',
                  fontSize: '0.95rem',
                  fontWeight: 'bold',
                },
              }}
            />
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <Box sx={{ mb: 3 }}>
              <Button 
                variant="contained" 
                color="primary"
                onClick={handleQuestionDialogOpen}
              >
                Create New Question
              </Button>
            </Box>

            <Grid container spacing={3} sx={{ mb: 3 }}>
              <Grid item xs={12} md={3}>
                <TextField
                  select
                  fullWidth
                  label="Topic"
                  variant="outlined"
                  value={topicFilter}
                  onChange={(e) => setTopicFilter(e.target.value)}
                >
                  <MenuItem value="">All Topics</MenuItem>
                  <MenuItem value="grammar">Grammar</MenuItem>
                  <MenuItem value="vocabulary">Vocabulary</MenuItem>
                  <MenuItem value="pronunciation">Pronunciation</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  select
                  fullWidth
                  label="Type"
                  variant="outlined"
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                >
                  <MenuItem value="">All Types</MenuItem>
                  <MenuItem value="FILL_IN_CHOICE">Multiple Choice</MenuItem>
                  <MenuItem value="FILL_IN_INPUT">Fill in the Blank</MenuItem>
                  <MenuItem value="SENTENCE_CONSTRUCTION">Sentence Construction</MenuItem>
                  <MenuItem value="AUDIO_RECOGNITION">Audio Recognition</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  select
                  fullWidth
                  label="Difficulty"
                  variant="outlined"
                  value={difficultyFilter}
                  onChange={(e) => setDifficultyFilter(e.target.value)}
                >
                  <MenuItem value="">All Difficulties</MenuItem>
                  <MenuItem value="EASY">Easy</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HARD">Hard</MenuItem>
                </TextField>
              </Grid>
            </Grid>

            <DataGrid
              rows={questionsData?.questions || []}
              columns={questionColumns}
              rowCount={questionsData?.totalQuestions || 0}
              loading={isQuestionsLoading}
              pageSizeOptions={[25, 50, 100]}
              paginationMode="server"
              paginationModel={{ page, pageSize }}
              onPaginationModelChange={(model) => {
                setPage(model.page);
                setPageSize(model.pageSize);
              }}
              disableRowSelectionOnClick
              sx={{
                height: 650,
                '& .MuiDataGrid-cell': {
                  fontSize: '0.95rem',
                },
                '& .MuiDataGrid-columnHeader': {
                  backgroundColor: '#f5f5f5',
                  fontSize: '0.95rem',
                  fontWeight: 'bold',
                },
              }}
            />
          </TabPanel>
        </CardContent>
      </Card>

      {/* Dialog for creating a new test */}
      <Dialog open={testDialogOpen} onClose={handleTestDialogClose} fullWidth maxWidth="md">
        <DialogTitle>Create New Test</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            Fill in the details to create a new test.
          </DialogContentText>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                margin="normal"
                label="Topic"
                name="topic"
                value={newTest.topic}
                onChange={handleTestChange}
                error={!!testErrors.topic}
                helperText={testErrors.topic}
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth margin="normal" error={!!testErrors.difficulty}>
                <InputLabel id="test-difficulty-label">Difficulty *</InputLabel>
                <Select
                  labelId="test-difficulty-label"
                  name="difficulty"
                  value={newTest.difficulty}
                  onChange={handleTestDifficultyChange}
                  label="Difficulty *"
                  required
                >
                  <MenuItem value="EASY">Easy</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HARD">Hard</MenuItem>
                </Select>
                {testErrors.difficulty && <FormHelperText>{testErrors.difficulty}</FormHelperText>}
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                margin="normal"
                label="Description"
                name="description"
                value={newTest.description}
                onChange={handleTestChange}
                multiline
                rows={4}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleTestDialogClose}>Cancel</Button>
          <Button onClick={handleSubmitTest} variant="contained" color="primary">
            Create Test
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog for creating a new question */}
      <Dialog open={questionDialogOpen} onClose={handleQuestionDialogClose} fullWidth maxWidth="md">
        <DialogTitle>Create New Question</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            Fill in the details to create a new question.
          </DialogContentText>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                margin="normal"
                label="Topic"
                name="topic"
                value={newQuestion.topic}
                onChange={handleQuestionChange}
                error={!!questionErrors.topic}
                helperText={questionErrors.topic}
                required
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth margin="normal" error={!!questionErrors.type}>
                <InputLabel id="question-type-label">Type *</InputLabel>
                <Select
                  labelId="question-type-label"
                  name="type"
                  value={newQuestion.type}
                  onChange={handleQuestionSelectChange}
                  label="Type *"
                  required
                >
                  <MenuItem value="FILL_IN_CHOICE">Multiple Choice</MenuItem>
                  <MenuItem value="FILL_IN_INPUT">Fill in the Blank</MenuItem>
                  <MenuItem value="SENTENCE_CONSTRUCTION">Sentence Construction</MenuItem>
                  <MenuItem value="AUDIO_RECOGNITION">Audio Recognition</MenuItem>
                </Select>
                {questionErrors.type && <FormHelperText>{questionErrors.type}</FormHelperText>}
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth margin="normal" error={!!questionErrors.difficulty}>
                <InputLabel id="question-difficulty-label">Difficulty *</InputLabel>
                <Select
                  labelId="question-difficulty-label"
                  name="difficulty"
                  value={newQuestion.difficulty}
                  onChange={handleQuestionSelectChange}
                  label="Difficulty *"
                  required
                >
                  <MenuItem value="EASY">Easy</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HARD">Hard</MenuItem>
                </Select>
                {questionErrors.difficulty && <FormHelperText>{questionErrors.difficulty}</FormHelperText>}
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                margin="normal"
                label="Question Content"
                name="content"
                value={newQuestion.content}
                onChange={handleQuestionChange}
                error={!!questionErrors.content}
                helperText={questionErrors.content}
                multiline
                rows={3}
                required
              />
            </Grid>
            
            {newQuestion.type === 'FILL_IN_CHOICE' && (
              <>
                <Grid item xs={12}>
                  <Typography variant="subtitle1" gutterBottom>
                    Options
                  </Typography>
                  {questionErrors.options && (
                    <FormHelperText error>{questionErrors.options}</FormHelperText>
                  )}
                </Grid>
                {[0, 1, 2, 3].map((index) => (
                  <Grid item xs={12} md={6} key={`option-${index}`}>
                    <TextField
                      fullWidth
                      label={`Option ${index + 1}`}
                      value={newQuestion.options?.[index] || ''}
                      onChange={(e) => handleOptionChange(index, e.target.value)}
                      required
                    />
                  </Grid>
                ))}
              </>
            )}
            
            <Grid item xs={12}>
              <TextField
                fullWidth
                margin="normal"
                label="Correct Answer"
                name="correctAnswer"
                value={newQuestion.correctAnswer}
                onChange={handleQuestionChange}
                error={!!questionErrors.correctAnswer}
                helperText={questionErrors.correctAnswer}
                required
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleQuestionDialogClose}>Cancel</Button>
          <Button onClick={handleSubmitQuestion} variant="contained" color="primary">
            Create Question
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
} 