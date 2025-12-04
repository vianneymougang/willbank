// ============================================
// WILLBANK - Transactions Screen
// ============================================

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
  TextInput,
} from 'react-native';
import { router } from 'expo-router';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { Card } from '@/presentation/components/ui/Card';
import { spacing, radius, shadows } from '@/config/theme';
import { Transaction } from '@/core/entities';
import { dashboardService } from '@/data/services/dashboardService';
import { TRANSACTION_TYPES } from '@/config/constants';

type FilterType = 'all' | 'transfer' | 'deposit' | 'withdrawal' | 'payment';

export default function TransactionsScreen() {
  const colors = useThemeColors();
  
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [filteredTransactions, setFilteredTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');

  useEffect(() => {
    loadTransactions();
  }, []);

  useEffect(() => {
    filterTransactions();
  }, [transactions, searchQuery, activeFilter]);

  const loadTransactions = async () => {
    try {
      const data = await dashboardService.getTransactions();
      setTransactions(data);
    } catch (error) {
      console.error('Failed to load transactions:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const filterTransactions = () => {
    let filtered = [...transactions];

    // Filter by type
    if (activeFilter !== 'all') {
      filtered = filtered.filter(t => t.type === activeFilter);
    }

    // Filter by search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        t =>
          t.description.toLowerCase().includes(query) ||
          t.recipientName?.toLowerCase().includes(query) ||
          t.merchantName?.toLowerCase().includes(query)
      );
    }

    setFilteredTransactions(filtered);
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadTransactions();
    setRefreshing(false);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('fr-FR').format(Math.abs(amount)) + ' FCFA';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const groupTransactionsByDate = () => {
    const groups: { [key: string]: Transaction[] } = {};
    
    filteredTransactions.forEach(transaction => {
      const date = new Date(transaction.createdAt).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(transaction);
    });

    return Object.entries(groups).map(([date, items]) => ({
      date,
      formattedDate: formatDate(items[0].createdAt),
      transactions: items,
    }));
  };

  const filters: { key: FilterType; label: string }[] = [
    { key: 'all', label: 'Tout' },
    { key: 'transfer', label: 'Virements' },
    { key: 'deposit', label: 'Dépôts' },
    { key: 'withdrawal', label: 'Retraits' },
    { key: 'payment', label: 'Paiements' },
  ];

  if (isLoading) {
    return (
      <View style={[styles.loadingContainer, { backgroundColor: colors.background }]}>
        <Text style={[styles.loadingText, { color: colors.text.secondary }]}>
          Chargement...
        </Text>
      </View>
    );
  }

  const groupedTransactions = groupTransactionsByDate();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.title, { color: colors.text.primary }]}>
          Transactions
        </Text>
        <TouchableOpacity
          style={[styles.iconButton, { backgroundColor: colors.surface }]}
        >
          <MaterialIcons name="filter-list" size={22} color={colors.text.primary} />
        </TouchableOpacity>
      </View>

      {/* Search Bar */}
      <View style={[styles.searchContainer, { backgroundColor: colors.surface, borderColor: colors.border }]}>
        <MaterialIcons name="search" size={20} color={colors.text.muted} />
        <TextInput
          style={[styles.searchInput, { color: colors.text.primary }]}
          placeholder="Rechercher une transaction..."
          placeholderTextColor={colors.text.muted}
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
        {searchQuery.length > 0 && (
          <TouchableOpacity onPress={() => setSearchQuery('')}>
            <MaterialIcons name="close" size={20} color={colors.text.muted} />
          </TouchableOpacity>
        )}
      </View>

      {/* Filters */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filtersContainer}
      >
        {filters.map((filter, index) => (
          <TouchableOpacity
            key={filter.key}
            style={[
              styles.filterChip,
              {
                backgroundColor:
                  activeFilter === filter.key ? colors.primary[500] : colors.surface,
                borderColor:
                  activeFilter === filter.key ? colors.primary[500] : colors.border,
                marginRight: index < filters.length - 1 ? spacing.sm : 0,
              },
            ]}
            onPress={() => setActiveFilter(filter.key)}
          >
            <Text
              style={[
                styles.filterChipText,
                {
                  color:
                    activeFilter === filter.key ? '#fff' : colors.text.secondary,
                },
              ]}
            >
              {filter.label}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Transactions List */}
      <ScrollView
        style={styles.listContainer}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={colors.primary[500]}
          />
        }
      >
        {groupedTransactions.length === 0 ? (
          <View style={styles.emptyState}>
            <MaterialIcons name="receipt-long" size={64} color={colors.text.muted} />
            <Text style={[styles.emptyTitle, { color: colors.text.primary }]}>
              Aucune transaction
            </Text>
            <Text style={[styles.emptySubtitle, { color: colors.text.secondary }]}>
              {searchQuery || activeFilter !== 'all'
                ? 'Aucun résultat pour votre recherche'
                : 'Vos transactions apparaîtront ici'}
            </Text>
          </View>
        ) : (
          groupedTransactions.map(group => (
            <View key={group.date} style={styles.dateGroup}>
              <Text style={[styles.dateHeader, { color: colors.text.secondary }]}>
                {group.formattedDate}
              </Text>

              <Card variant="outlined" padding="none">
                {group.transactions.map((transaction, index) => {
                  const typeConfig = TRANSACTION_TYPES[transaction.type];
                  const isCredit = transaction.amount > 0;

                  return (
                    <TouchableOpacity
                      key={transaction.id}
                      style={[
                        styles.transactionItem,
                        index < group.transactions.length - 1 && {
                          borderBottomWidth: 1,
                          borderBottomColor: colors.divider,
                        },
                      ]}
                      onPress={() => router.push(`/transactions/${transaction.id}` as any)}
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
                        <Text style={[styles.transactionMeta, { color: colors.text.muted }]}>
                          {typeConfig.label} • {formatTime(transaction.createdAt)}
                        </Text>
                      </View>

                      <View style={styles.transactionRight}>
                        <Text
                          style={[
                            styles.transactionAmount,
                            { color: isCredit ? colors.success : colors.text.primary },
                          ]}
                        >
                          {isCredit ? '+' : '-'}{formatCurrency(transaction.amount)}
                        </Text>
                        <View
                          style={[
                            styles.statusBadge,
                            {
                              backgroundColor:
                                transaction.status === 'completed'
                                  ? colors.success + '15'
                                  : transaction.status === 'pending'
                                  ? colors.warning + '15'
                                  : colors.error + '15',
                            },
                          ]}
                        >
                          <Text
                            style={[
                              styles.statusText,
                              {
                                color:
                                  transaction.status === 'completed'
                                    ? colors.success
                                    : transaction.status === 'pending'
                                    ? colors.warning
                                    : colors.error,
                              },
                            ]}
                          >
                            {transaction.status === 'completed'
                              ? 'Effectué'
                              : transaction.status === 'pending'
                              ? 'En cours'
                              : 'Échoué'}
                          </Text>
                        </View>
                      </View>
                    </TouchableOpacity>
                  );
                })}
              </Card>
            </View>
          ))
        )}

        {/* Bottom Spacing */}
        <View style={{ height: spacing.xxl }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
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
    paddingTop: spacing.xxl + spacing.lg,
    marginBottom: spacing.md,
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
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: spacing.lg,
    paddingHorizontal: spacing.md,
    height: 48,
    borderRadius: radius.md,
    borderWidth: 1,
    marginBottom: spacing.md,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    marginLeft: spacing.sm,
  },
  filtersContainer: {
    paddingHorizontal: spacing.lg,
    paddingRight: spacing.lg,
    marginBottom: spacing.md,
    alignItems: 'center',
  },
  filterChip: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: radius.full,
    borderWidth: 1,
  },
  filterChipText: {
    fontSize: 14,
    fontWeight: '500',
  },
  listContainer: {
    flex: 1,
  },
  listContent: {
    paddingHorizontal: spacing.lg,
  },
  dateGroup: {
    marginBottom: spacing.lg,
  },
  dateHeader: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: spacing.sm,
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
  transactionMeta: {
    fontSize: 12,
  },
  transactionRight: {
    alignItems: 'flex-end',
  },
  transactionAmount: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
  },
  statusBadge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: radius.full,
  },
  statusText: {
    fontSize: 10,
    fontWeight: '500',
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.xxl * 2,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginTop: spacing.md,
    marginBottom: spacing.xs,
  },
  emptySubtitle: {
    fontSize: 14,
    textAlign: 'center',
  },
});
