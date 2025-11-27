// ============================================
// WILLBANK - Card Component
// ============================================

import React, { ReactNode } from 'react';
import { View, StyleSheet, TouchableOpacity, ViewStyle } from 'react-native';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { spacing, radius, shadows } from '@/config/theme';

interface CardProps {
  children: ReactNode;
  onPress?: () => void;
  style?: ViewStyle;
  variant?: 'default' | 'elevated' | 'outlined';
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

export function Card({
  children,
  onPress,
  style,
  variant = 'default',
  padding = 'md',
}: CardProps) {
  const colors = useThemeColors();

  const getPadding = () => {
    switch (padding) {
      case 'none':
        return 0;
      case 'sm':
        return spacing.sm;
      case 'lg':
        return spacing.lg;
      default:
        return spacing.md;
    }
  };

  const getVariantStyle = (): ViewStyle => {
    switch (variant) {
      case 'elevated':
        return {
          ...shadows.md,
          borderWidth: 0,
        };
      case 'outlined':
        return {
          borderWidth: 1,
          borderColor: colors.border,
        };
      default:
        return {
          ...shadows.sm,
        };
    }
  };

  const cardStyle: ViewStyle = {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    padding: getPadding(),
    ...getVariantStyle(),
  };

  if (onPress) {
    return (
      <TouchableOpacity
        onPress={onPress}
        activeOpacity={0.7}
        style={[cardStyle, style]}
      >
        {children}
      </TouchableOpacity>
    );
  }

  return <View style={[cardStyle, style]}>{children}</View>;
}

// Account Card - Specialized card for displaying bank accounts
interface AccountCardProps {
  accountNumber: string;
  accountType: string;
  balance: number;
  currency?: string;
  onPress?: () => void;
  isPrimary?: boolean;
}

export function AccountCard({
  accountNumber,
  accountType,
  balance,
  currency = 'FCFA',
  onPress,
  isPrimary = false,
}: AccountCardProps) {
  const colors = useThemeColors();

  const formatBalance = (amount: number) => {
    return new Intl.NumberFormat('fr-FR').format(amount);
  };

  const maskAccountNumber = (num: string) => {
    if (num.length <= 4) return num;
    return `•••• ${num.slice(-4)}`;
  };

  return (
    <TouchableOpacity
      onPress={onPress}
      activeOpacity={0.8}
      style={[
        styles.accountCard,
        {
          backgroundColor: isPrimary ? colors.primary[500] : colors.surface,
          borderColor: isPrimary ? 'transparent' : colors.border,
        },
      ]}
    >
      <View style={styles.accountCardHeader}>
        <View
          style={[
            styles.accountTypeBadge,
            {
              backgroundColor: isPrimary
                ? 'rgba(255,255,255,0.2)'
                : colors.primary[50],
            },
          ]}
        >
          <View
            style={[
              styles.accountTypeText,
              {
                color: isPrimary ? colors.white : colors.primary[500],
              },
            ]}
          />
        </View>
      </View>

      <View style={styles.accountCardContent}>
        <View style={styles.accountInfo}>
          <View
            style={[
              styles.accountTypeLabel,
              { color: isPrimary ? 'rgba(255,255,255,0.7)' : colors.text.secondary },
            ]}
          />
          <View
            style={[
              styles.accountNumberText,
              { color: isPrimary ? colors.white : colors.text.primary },
            ]}
          />
        </View>

        <View style={styles.balanceContainer}>
          <View
            style={[
              styles.balanceLabel,
              { color: isPrimary ? 'rgba(255,255,255,0.7)' : colors.text.secondary },
            ]}
          />
          <View
            style={[
              styles.balanceAmount,
              { color: isPrimary ? colors.white : colors.text.primary },
            ]}
          />
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  accountCard: {
    borderRadius: radius.xl,
    padding: spacing.lg,
    borderWidth: 1,
    minHeight: 160,
    ...shadows.md,
  },
  accountCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  accountTypeBadge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: radius.full,
  },
  accountTypeText: {
    fontSize: 12,
    fontWeight: '600',
  },
  accountCardContent: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  accountInfo: {
    marginBottom: spacing.md,
  },
  accountTypeLabel: {
    fontSize: 12,
    marginBottom: 2,
  },
  accountNumberText: {
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 2,
  },
  balanceContainer: {},
  balanceLabel: {
    fontSize: 12,
    marginBottom: 2,
  },
  balanceAmount: {
    fontSize: 28,
    fontWeight: '700',
  },
});
