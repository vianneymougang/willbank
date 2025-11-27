// ============================================
// WILLBANK - App Entry Point
// ============================================

import { useEffect } from 'react';
import { router } from 'expo-router';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { useAuth } from '@/presentation/providers/AuthProvider';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { radius } from '@/config/theme';

export default function Index() {
  const { isAuthenticated, isLoading } = useAuth();
  const colors = useThemeColors();

  useEffect(() => {
    if (!isLoading) {
      if (isAuthenticated) {
        router.replace('/(tabs)');
      } else {
        router.replace('/(auth)/login');
      }
    }
  }, [isAuthenticated, isLoading]);

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.logoContainer, { backgroundColor: colors.primary[500] }]}>
        <Text style={styles.logoText}>W</Text>
      </View>
      <Text style={[styles.appName, { color: colors.text.primary }]}>WillBank</Text>
      <ActivityIndicator 
        size="large" 
        color={colors.primary[500]} 
        style={styles.loader}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoContainer: {
    width: 100,
    height: 100,
    borderRadius: radius.xxl,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  logoText: {
    fontSize: 48,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  appName: {
    fontSize: 28,
    fontWeight: '700',
    marginBottom: 32,
  },
  loader: {
    marginTop: 16,
  },
});
