import { Grid, IconButton, Typography } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

type ManageBreadCardProps = {
  img: string;
  name: string;
};

const ManageBreadCard: React.FC<ManageBreadCardProps> = ({ img, name }) => {
  return (
    <Grid
      item
      xs={4}
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: 300,
        flexWrap: 'nowrap',
      }}
    >
      <Grid
        container
        width={230}
        height={260}
        boxShadow={'1px 1px 5px 0px rgba(0, 0, 0, 0.05)'}
        borderRadius={5}
        justifyContent="center"
        alignItems="center"
        position="relative"
      >
        <img src={img} alt=" hi" width={160} height={160} />
        <Typography fontWeight={'bold'}>{name}</Typography>
        <IconButton sx={{ color: '#dddddd', padding: 0.5 }}>
          <EditIcon fontSize="small" sx={{ width: '16px', height: '16px' }} />
        </IconButton>
      </Grid>
    </Grid>
  );
};

export default ManageBreadCard;
