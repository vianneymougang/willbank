// ============================================
// WILLBANK - Accounts Screen
// ============================================

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { router } from 'expo-router';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { Card } from '@/presentation/components/ui/Card';
import { spacing, radius, shadows } from '@/config/theme';
import { Account } from '@/core/entities';
import { dashboardService } from '@/data/services/dashboardService';
import { ACCOUNT_TYPES } from '@/config/constants';

export default function AccountsScreen() {
  const colors = useThemeColors();
  
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [hideBalances, setHideBalances] = useState(false);

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    try {
      const data = await dashboardService.getAccounts();
      setAccounts(data);
    } catch (error) {
      console.error('Failed to load accounts:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadAccounts();
    setRefreshing(false);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('fr-FR').format(amount) + ' FCFA';
  };

  const getTotalBalance = () => {
    return accounts.reduce((sum, acc) => sum + acc.balance, 0);
  };

  const getAccountIcon = (type: Account['type']) => {
    return type === 'current' ? 'account-balance-wallet' : 'savings';
  };

  if (isLoading) {
    return (
      <View style={[styles.loadingContainer, { backgroundColor: colors.background }]}>
        <Text style={[styles.loadingText, { color: colors.text.secondary }]}>
          Chargement...
        </Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
      refreshControl={
        <RefreshControl
          refreshing={refreshing}
          onRefresh={onRefresh}
          tintColor={colors.primary[500]}
        />
      }
    >
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.title, { color: colors.text.primary }]}>
          Mes comptes
        </Text>
        <TouchableOpacity
          style={[styles.iconButton, { backgroundColor: colors.surface }]}
          onPress={() => setHideBalances(!hideBalances)}
        >
          <MaterialIcons
            name={hideBalances ? 'visibility-off' : 'visibility'}
            size={22}
            color={colors.text.primary}
          />
        </TouchableOpacity>
      </View>

      {/* Total Balance Card */}
      <View style={[styles.totalCard, { backgroundColor: colors.primary[500] }]}>
        <Text style={styles.totalLabel}>Solde total</Text>
        <Text style={styles.totalAmount}>
          {hideBalances ? '••••••••' : formatCurrency(getTotalBalance())}
        </Text>
        <View style={styles.totalFooter}>
          <MaterialIcons name="account-balance" size={16} color="rgba(255,255,255,0.7)" />
          <Text style={styles.totalFooterText}>
            {accounts.length} compte{accounts.length > 1 ? 's' : ''} actif{accounts.length > 1 ? 's' : ''}
          </Text>
        </View>
      </View>

      {/* Accounts List */}
      <View style={styles.accountsList}>
        {accounts.map((account) => (
          <TouchableOpacity
            key={account.id}
            style={[styles.accountCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
            onPress={() => router.push(`/accounts/${account.id}`)}
            activeOpacity={0.7}
          >
            <View style={styles.accountCardLeft}>
              <View style={[styles.accountIcon, { backgroundColor: colors.primary[50] }]}>
                <MaterialIcons
                  name={getAccountIcon(account.type) as any}
                  size={24}
                  color={colors.primary[500]}
                />
              </View>
              <View style={styles.accountInfo}>
                <Text style={[styles.accountLabel, { color: colors.text.primary }]}>
                  {account.label}
                </Text>
                <Text style={[styles.accountType, { color: colors.text.secondary }]}>
                  {ACCOUNT_TYPES[account.type].label}
                </Text>
                <Text style={[styles.accountNumber, { color: colors.text.muted }]}>
                  •••• {account.accountNumber.slice(-4)}
                </Text>
              </View>
            </View>

            <View style={styles.accountCardRight}>
              <Text style={[styles.accountBalance, { color: colors.text.primary }]}>
                {hideBalances ? '••••••' : formatCurrency(account.balance)}
              </Text>
              <View style={styles.statusContainer}>
                <View
                  style={[
                    styles.statusDot,
                    { backgroundColor: account.status === 'active' ? colors.success : colors.warning },
                  ]}
                />
                <Text style={[styles.statusText, { color: colors.text.muted }]}>
                  {account.status === 'active' ? 'Actif' : 'Bloqué'}
                </Text>
              </View>
              <MaterialIcons name="chevron-right" size={20} color={colors.text.muted} />
            </View>
          </TouchableOpacity>
        ))}
      </View>

      {/* Quick Actions */}
      <View style={styles.actionsSection}>
        <Text style={[styles.sectionTitle, { color: colors.text.primary }]}>
          Actions rapides
        </Text>
        <View style={styles.actionsGrid}>
          <TouchableOpacity
            style={[styles.actionCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
          >
            <View style={[styles.actionIcon, { backgroundColor: colors.info + '15' }]}>
              <MaterialIcons name="add" size={24} color={colors.info} />
            </View>
            <Text style={[styles.actionLabel, { color: colors.text.primary }]}>
              Nouveau compte
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
          >
            <View style={[styles.actionIcon, { backgroundColor: colors.secondary[500] + '15' }]}>
              <MaterialIcons name="download" size={24} color={colors.secondary[500]} />
            </View>
            <Text style={[styles.actionLabel, { color: colors.text.primary }]}>
              Télécharger RIB
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
          >
            <View style={[styles.actionIcon, { backgroundColor: colors.accent[500] + '15' }]}>
              <MaterialIcons name="description" size={24} color={colors.accent[500]} />
            </View>
            <Text style={[styles.actionLabel, { color: colors.text.primary }]}>
              Relevé de compte
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
          >
            <View style={[styles.actionIcon, { backgroundColor: colors.error + '15' }]}>
              <MaterialIcons name="block" size={24} color={colors.error} />
            </View>
            <Text style={[styles.actionLabel, { color: colors.text.primary }]}>
              Bloquer carte
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Bottom Spacing */}
      <View style={{ height: spacing.xxl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    paddingTop: spacing.xxl + spacing.lg,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadingText: {
    fontSize: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.lg,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
  },
  iconButton: {
    width: 44,
    height: 44,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  totalCard: {
    marginHorizontal: spacing.lg,
    borderRadius: radius.xl,
    padding: spacing.lg,
    marginBottom: spacing.xl,
    ...shadows.lg,
  },
  totalLabel: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 14,
    marginBottom: spacing.xs,
  },
  totalAmount: {
    color: '#fff',
    fontSize: 32,
    fontWeight: '700',
    marginBottom: spacing.md,
  },
  totalFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  totalFooterText: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 12,
  },
  accountsList: {
    paddingHorizontal: spacing.lg,
    gap: spacing.md,
    marginBottom: spacing.xl,
  },
  accountCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderRadius: radius.lg,
    padding: spacing.md,
    borderWidth: 1,
    ...shadows.sm,
  },
  accountCardLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  accountIcon: {
    width: 48,
    height: 48,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  accountInfo: {
    flex: 1,
  },
  accountLabel: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 2,
  },
  accountType: {
    fontSize: 12,
    marginBottom: 2,
  },
  accountNumber: {
    fontSize: 12,
    letterSpacing: 1,
  },
  accountCardRight: {
    alignItems: 'flex-end',
  },
  accountBalance: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 4,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginBottom: 4,
  },
  statusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  statusText: {
    fontSize: 10,
  },
  actionsSection: {
    paddingHorizontal: spacing.lg,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: spacing.md,
  },
  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.md,
  },
  actionCard: {
    width: '47%',
    borderRadius: radius.lg,
    padding: spacing.md,
    borderWidth: 1,
    alignItems: 'center',
    ...shadows.sm,
  },
  actionIcon: {
    width: 48,
    height: 48,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.sm,
  },
  actionLabel: {
    fontSize: 12,
    fontWeight: '500',
    textAlign: 'center',
  },
});
