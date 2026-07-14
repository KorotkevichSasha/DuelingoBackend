import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  MenuItem,
  Button,
  IconButton,
  Menu,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';
import type { GridColDef, GridValueGetterParams, GridRowParams } from '@mui/x-data-grid';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { fetchUsers, updateUserRole, resetUserPassword } from '../api/users';
import { MoreVert as MoreVertIcon, Edit as EditIcon, LockReset as LockResetIcon } from '@mui/icons-material';

export default function Users() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(25);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [roleDialogOpen, setRoleDialogOpen] = useState(false);
  const [passwordResetDialogOpen, setPasswordResetDialogOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<{ id: string; username: string; role: string } | null>(null);
  const [newRole, setNewRole] = useState('');
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ['users', page, pageSize, searchQuery, roleFilter],
    queryFn: () => fetchUsers(page, pageSize, searchQuery, roleFilter),
  });

  const handleRoleDialogOpen = (user: { id: string; username: string; role: string }) => {
    setSelectedUser(user);
    setNewRole(user.role);
    setRoleDialogOpen(true);
  };

  const handleRoleDialogClose = () => {
    setRoleDialogOpen(false);
    setSelectedUser(null);
  };

  const handlePasswordResetDialogOpen = (user: { id: string; username: string }) => {
    setSelectedUser(user);
    setPasswordResetDialogOpen(true);
  };

  const handlePasswordResetDialogClose = () => {
    setPasswordResetDialogOpen(false);
    setSelectedUser(null);
  };

  const handleRoleChange = async () => {
    if (!selectedUser) return;
    
    try {
      await updateUserRole(selectedUser.id, newRole);
      enqueueSnackbar('User role updated successfully', { variant: 'success' });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      handleRoleDialogClose();
    } catch (error) {
      enqueueSnackbar('Failed to update user role', { variant: 'error' });
    }
  };

  const handlePasswordReset = async () => {
    if (!selectedUser) return;
    
    try {
      await resetUserPassword(selectedUser.id);
      enqueueSnackbar('Password reset successful', { variant: 'success' });
      handlePasswordResetDialogClose();
    } catch (error) {
      enqueueSnackbar('Failed to reset password', { variant: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'username', headerName: 'Username', flex: 1, minWidth: 150 },
    { field: 'email', headerName: 'Email', flex: 1.5, minWidth: 200 },
    { field: 'role', headerName: 'Role', flex: 0.8, minWidth: 100 },
    { field: 'points', headerName: 'Points', flex: 0.8, minWidth: 100 },
    { field: 'totalWords', headerName: 'Words Learned', flex: 1, minWidth: 130 },
    { field: 'completedTests', headerName: 'Tests Completed', flex: 1, minWidth: 130 },
    { field: 'totalDuels', headerName: 'Total Duels', flex: 1, minWidth: 120 },
    {
      field: 'lastLogin',
      headerName: 'Last Login',
      flex: 1.2,
      minWidth: 180,
      valueFormatter: (params) =>
        params.value ? new Date(params.value).toLocaleString() : 'Never',
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: 'Actions',
      width: 100,
      getActions: (params: GridRowParams) => [
        <GridActionsCellItem 
          icon={<EditIcon />} 
          label="Change Role" 
          onClick={() => handleRoleDialogOpen({
            id: params.row.id,
            username: params.row.username,
            role: params.row.role
          })}
          showInMenu
        />,
        <GridActionsCellItem 
          icon={<LockResetIcon />} 
          label="Reset Password" 
          onClick={() => handlePasswordResetDialogOpen({
            id: params.row.id,
            username: params.row.username
          })}
          showInMenu
        />
      ]
    },
  ];

  if (error) {
    return <Typography color="error">Error loading users</Typography>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        User Management
      </Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" gap={2} mb={2}>
            <TextField
              label="Search users"
              variant="outlined"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              sx={{ flexGrow: 1 }}
            />
            <TextField
              select
              label="Role"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              sx={{ width: 200 }}
            >
              <MenuItem value="">All Roles</MenuItem>
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </TextField>
          </Box>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
          <DataGrid
            rows={data?.users || []}
            columns={columns}
            rowCount={data?.totalUsers || 0}
            loading={isLoading}
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
        </CardContent>
      </Card>

      {/* Role Change Dialog */}
      <Dialog open={roleDialogOpen} onClose={handleRoleDialogClose}>
        <DialogTitle>Change User Role</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Change role for user: <strong>{selectedUser?.username}</strong>
          </DialogContentText>
          <FormControl fullWidth margin="normal">
            <InputLabel id="role-select-label">Role</InputLabel>
            <Select
              labelId="role-select-label"
              value={newRole}
              label="Role"
              onChange={(e) => setNewRole(e.target.value)}
            >
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleRoleDialogClose}>Cancel</Button>
          <Button onClick={handleRoleChange} variant="contained" color="primary">
            Update Role
          </Button>
        </DialogActions>
      </Dialog>

      {/* Password Reset Dialog */}
      <Dialog open={passwordResetDialogOpen} onClose={handlePasswordResetDialogClose}>
        <DialogTitle>Reset User Password</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to reset the password for <strong>{selectedUser?.username}</strong>?
            This will send a password reset link to the user's email.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handlePasswordResetDialogClose}>Cancel</Button>
          <Button onClick={handlePasswordReset} variant="contained" color="primary">
            Reset Password
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
} 