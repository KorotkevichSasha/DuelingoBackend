import { Alert, Box, Chip, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { useQuery } from '@tanstack/react-query';
import { fetchReports } from '../api/reports';

export default function Reports() {
  const { data = [], isLoading, error } = useQuery({ queryKey: ['reports'], queryFn: fetchReports });
  const columns: GridColDef[] = [
    { field: 'reportedUsername', headerName: 'Reported user', flex: 1, minWidth: 160 },
    { field: 'reporterUsername', headerName: 'Reporter', flex: 1, minWidth: 160 },
    { field: 'reason', headerName: 'Reason', flex: 1, minWidth: 200, renderCell: ({ value }) => <Chip label={value} color="warning" size="small" /> },
    { field: 'createdAt', headerName: 'Created', flex: 1, minWidth: 190, valueFormatter: ({ value }) => new Date(value).toLocaleString() },
  ];
  return <Box sx={{ height: 650, width: '100%' }}>
    <Typography variant="h4" mb={2}>User reports</Typography>
    {error && <Alert severity="error">Could not load moderation reports.</Alert>}
    <DataGrid rows={data} columns={columns} loading={isLoading} disableRowSelectionOnClick />
  </Box>;
}
