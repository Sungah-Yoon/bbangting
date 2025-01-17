import { Grid } from '@mui/material';
import StoreCard from './StoreCard';
import store from '../json/store.json';

const StoreContainer = () => {
  return (
    <Grid container alignItems="center" height={'100%'} mt={5}>
      {store.map(store => (
        <StoreCard
          storeId={store.storeId}
          img={store.storeLogo}
          name={store.storeName}
        />
      ))}
    </Grid>
  );
};

export default StoreContainer;
