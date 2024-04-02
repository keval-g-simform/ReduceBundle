import React, { useState } from 'react';
import {
  ActivityIndicator,
  Button,
  Image,
  NativeModules,
  SafeAreaView,
  StyleSheet,
  Text,
} from 'react-native';
import Asset from './asset';

function App(): React.JSX.Element {
  const { ReduceBundleManager } = NativeModules;
  const [imageUri, setImageUri] = useState('');
  const [loading, setLoading] = useState(false);

  const getImage = () => {
    setLoading(true);
    ReduceBundleManager.loadImageWithTag('profile', 'png')
      .then((imagePath: string) => {
        setImageUri(imagePath);
        setLoading(false);
      })
      .catch((e: any) => {
        setImageUri(e.toString());
        setLoading(false);
      });
  };

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.title}>Play Asset Delivery</Text>
      <Button title="get Image" onPress={getImage} />
      {loading ? (
        <ActivityIndicator animating={loading} />
      ) : (
        <Image
          source={{
            uri: `data:image/png;base64,${imageUri}`,
          }}
          style={styles.image}
        />
      )}
      <Text style={styles.subTitle}>
        {'Image from React Native Asset folder'}
      </Text>
      <Image source={Asset.profile} style={styles.image} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
  },
  subContainer: {
    alignItems: 'center',
  },
  title: {
    fontSize: 25,
    fontWeight: '700',
    color: 'black',
    marginVertical: 10,
  },
  subTitle: {
    fontSize: 17,
    fontWeight: '400',
    color: 'black',
    marginTop: 35,
  },
  image: {
    width: 280,
    height: 280,
    marginTop: 20,
  },
});

export default App;
