// ============================================
// WILLBANK - Dashboard Screen
// ============================================

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
  Dimensions,
} from 'react-native';
import { router } from 'expo-router';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { useAuth } from '@/presentation/providers/AuthProvider';
import { Card } from '@/presentation/components/ui/Card';
import { spacing, radius, shadows } from '@/config/theme';
import { Account, Transaction, DashboardData } from '@/core/entities';
import { dashboardService } from '@/data/services/dashboardService';
import { TRANSACTION_TYPES, ACCOUNT_TYPES } from '@/config/constants';

const { width } = Dimensions.get('window');
const CARD_WIDTH = width - spacing.lg * 2;

export default function DashboardScreen() {
  const colors = useThemeColors();
  const { user } = useAuth();
  
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [hideBalance, setHideBalance] = useState(false);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const data = await dashboardService.getDashboard();
      setDashboardData(data);
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadDashboard();
    setRefreshing(false);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('fr-FR').format(Math.abs(amount)) + ' FCFA';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) return "Aujourd'hui";
    if (days === 1) return 'Hier';
    if (days < 7) return `Il y a ${days} jours`;
    
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Bonjour';
    if (hour < 18) return 'Bon après-midi';
    return 'Bonsoir';
  };

  const quickActions = [
    { icon: 'swap-horiz', label: 'Virement', color: colors.info, route: '/transfer' },
    { icon: 'qr-code-scanner', label: 'Scanner', color: colors.secondary[500], route: '/scan' },
    { icon: 'account-balance', label: 'Dépôt', color: colors.success, route: '/deposit' },
    { icon: 'receipt-long', label: 'Factures', color: colors.accent[500], route: '/bills' },
  ];

  if (isLoading) {
    return (
      <View style={[styles.loadingContainer, { backgroundColor: colors.background }]}>
        <View style={[styles.logoContainer, { backgroundColor: colors.primary[500] }]}>
          <Text style={styles.logoText}>W</Text>
        </View>
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
        <View style={styles.headerLeft}>
          <Text style={[styles.greeting, { color: colors.text.secondary }]}>
            {getGreeting()},
          </Text>
          <Text style={[styles.userName, { color: colors.text.primary }]}>
            {dashboardData?.user.firstName || 'Utilisateur'} 👋
          </Text>
        </View>
        <View style={styles.headerRight}>
          <TouchableOpacity
            style={[styles.iconButton, { backgroundColor: colors.surface }]}
            onPress={() => router.push('/notifications')}
          >
            <MaterialIcons name="notifications-none" size={24} color={colors.text.primary} />
            {(dashboardData?.unreadNotifications ?? 0) > 0 && (
              <View style={[styles.badge, { backgroundColor: colors.error }]}>
                <Text style={styles.badgeText}>
                  {dashboardData?.unreadNotifications}
                </Text>
              </View>
            )}
          </TouchableOpacity>
        </View>
      </View>

      {/* Balance Card */}
      <View style={[styles.balanceCard, { backgroundColor: colors.primary[500] }]}>
        <View style={styles.balanceHeader}>
          <Text style={styles.balanceLabel}>Solde total</Text>
          <TouchableOpacity onPress={() => setHideBalance(!hideBalance)}>
            <MaterialIcons
              name={hideBalance ? 'visibility-off' : 'visibility'}
              size={22}
              color="rgba(255,255,255,0.7)"
            />
          </TouchableOpacity>
        </View>
        <Text style={styles.balanceAmount}>
          {hideBalance ? '••••••••' : formatCurrency(dashboardData?.totalBalance || 0)}
        </Text>
        <View style={styles.balanceFooter}>
          <View style={styles.accountsCount}>
            <MaterialIcons name="account-balance-wallet" size={16} color="rgba(255,255,255,0.7)" />
            <Text style={styles.accountsCountText}>
              {dashboardData?.accounts.length || 0} compte(s)
            </Text>
          </View>
        </View>
      </View>

      {/* Quick Actions */}
      <View style={styles.quickActions}>
        {quickActions.map((action, index) => (
          <TouchableOpacity
            key={index}
            style={styles.quickActionItem}
            onPress={() => {}}
          >
            <View style={[styles.quickActionIcon, { backgroundColor: action.color + '15' }]}>
              <MaterialIcons name={action.icon as any} size={24} color={action.color} />
            </View>
            <Text style={[styles.quickActionLabel, { color: colors.text.secondary }]}>
              {action.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Accounts Section */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={[styles.sectionTitle, { color: colors.text.primary }]}>
            Mes comptes
          </Text>
          <TouchableOpacity onPress={() => router.push('/(tabs)/accounts')}>
            <Text style={[styles.seeAll, { color: colors.primary[500] }]}>
              Voir tout
            </Text>
          </TouchableOpacity>
        </View>

        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.accountsScroll}
        >
          {dashboardData?.accounts.map((account, index) => (
            <TouchableOpacity
              key={account.id}
              style={[
                styles.accountCard,
                {
                  backgroundColor: index === 0 ? colors.primary[500] : colors.surface,
                  borderColor: index === 0 ? 'transparent' : colors.border,
                },
              ]}
              onPress={() => router.push(`/accounts/${account.id}`)}
            >
              <View style={styles.accountCardHeader}>
                <View
                  style={[
                    styles.accountTypeBadge,
                    {
                      backgroundColor: index === 0
                        ? 'rgba(255,255,255,0.2)'
                        : colors.primary[50],
                    },
                  ]}
                >
                  <Text
                    style={[
                      styles.accountTypeText,
                      { color: index === 0 ? '#fff' : colors.primary[500] },
                    ]}
                  >
                    {ACCOUNT_TYPES[account.type].label}
                  </Text>
                </View>
              </View>

              <View style={styles.accountCardContent}>
                <Text
                  style={[
                    styles.accountLabel,
                    { color: index === 0 ? 'rgba(255,255,255,0.7)' : colors.text.secondary },
                  ]}
                >
                  {account.label}
                </Text>
                <Text
                  style={[
                    styles.accountNumber,
                    { color: index === 0 ? 'rgba(255,255,255,0.8)' : colors.text.muted },
                  ]}
                >
                  •••• {account.accountNumber.slice(-4)}
                </Text>
              </View>

              <Text
                style={[
                  styles.accountBalance,
                  { color: index === 0 ? '#fff' : colors.text.primary },
                ]}
              >
                {hideBalance ? '••••••' : formatCurrency(account.balance)}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Recent Transactions */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={[styles.sectionTitle, { color: colors.text.primary }]}>
            Transactions récentes
          </Text>
          <TouchableOpacity onPress={() => router.push('/(tabs)/transactions')}>
            <Text style={[styles.seeAll, { color: colors.primary[500] }]}>
              Voir tout
            </Text>
          </TouchableOpacity>
        </View>

        <Card variant="outlined" padding="none">
          {dashboardData?.recentTransactions.slice(0, 5).map((transaction, index) => {
            const typeConfig = TRANSACTION_TYPES[transaction.type];
            const isCredit = transaction.amount > 0;

            return (
              <TouchableOpacity
                key={transaction.id}
                style={[
                  styles.transactionItem,
                  index < (dashboardData?.recentTransactions.length || 0) - 1 && {
                    borderBottomWidth: 1,
                    borderBottomColor: colors.divider,
                  },
                ]}
                onPress={() => router.push(`/transactions/${transaction.id}`)}
              >
                <View
                  style={[
                    styles.transactionIcon,
                    { backgroundColor: typeConfig.color + '15' },
                  ]}
                >
                  <MaterialIcons
                    name={typeConfig.icon as any}
                    size={20}
                    color={typeConfig.color}
                  />
                </View>

                <View style={styles.transactionInfo}>
                  <Text style={[styles.transactionDesc, { color: colors.text.primary }]}>
                    {transaction.description}
                  </Text>
                  <Text style={[styles.transactionDate, { color: colors.text.muted }]}>
                    {formatDate(transaction.createdAt)}
                  </Text>
                </View>

                <Text
                  style={[
                    styles.transactionAmount,
                    { color: isCredit ? colors.success : colors.text.primary },
                  ]}
                >
                  {isCredit ? '+' : '-'}{formatCurrency(transaction.amount)}
                </Text>
              </TouchableOpacity>
            );
          })}
        </Card>
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
  logoContainer: {
    width: 80,
    height: 80,
    borderRadius: radius.xl,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.md,
  },
  logoText: {
    fontSize: 40,
    fontWeight: '700',
    color: '#FFFFFF',
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
  headerLeft: {},
  headerRight: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  greeting: {
    fontSize: 14,
  },
  userName: {
    fontSize: 24,
    fontWeight: '700',
  },
  iconButton: {
    width: 44,
    height: 44,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  badge: {
    position: 'absolute',
    top: 6,
    right: 6,
    minWidth: 18,
    height: 18,
    borderRadius: 9,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 4,
  },
  badgeText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: '700',
  },
  balanceCard: {
    marginHorizontal: spacing.lg,
    borderRadius: radius.xl,
    padding: spacing.lg,
    marginBottom: spacing.lg,
    ...shadows.lg,
  },
  balanceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  balanceLabel: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 14,
  },
  balanceAmount: {
    color: '#fff',
    fontSize: 36,
    fontWeight: '700',
    marginBottom: spacing.md,
  },
  balanceFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  accountsCount: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  accountsCountText: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 12,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.xl,
  },
  quickActionItem: {
    alignItems: 'center',
  },
  quickActionIcon: {
    width: 56,
    height: 56,
    borderRadius: radius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.xs,
  },
  quickActionLabel: {
    fontSize: 12,
    fontWeight: '500',
  },
  section: {
    marginBottom: spacing.lg,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  seeAll: {
    fontSize: 14,
    fontWeight: '500',
  },
  accountsScroll: {
    paddingHorizontal: spacing.lg,
    gap: spacing.md,
  },
  accountCard: {
    width: 200,
    borderRadius: radius.xl,
    padding: spacing.md,
    borderWidth: 1,
    ...shadows.sm,
  },
  accountCardHeader: {
    marginBottom: spacing.md,
  },
  accountTypeBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: radius.full,
  },
  accountTypeText: {
    fontSize: 10,
    fontWeight: '600',
  },
  accountCardContent: {
    marginBottom: spacing.sm,
  },
  accountLabel: {
    fontSize: 12,
    marginBottom: 2,
  },
  accountNumber: {
    fontSize: 14,
    fontWeight: '500',
    letterSpacing: 1,
  },
  accountBalance: {
    fontSize: 20,
    fontWeight: '700',
  },
  transactionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: spacing.md,
  },
  transactionIcon: {
    width: 44,
    height: 44,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  transactionInfo: {
    flex: 1,
  },
  transactionDesc: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 2,
  },
  transactionDate: {
    fontSize: 12,
  },
  transactionAmount: {
    fontSize: 14,
    fontWeight: '600',
  },
});
