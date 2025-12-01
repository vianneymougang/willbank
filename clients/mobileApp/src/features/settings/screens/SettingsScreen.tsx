// ============================================
// WILLBANK - Settings Screen
// ============================================

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
  Alert,
} from 'react-native';
import { router } from 'expo-router';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useThemeColors, useTheme } from '@/presentation/providers/ThemeProvider';
import { useAuth } from '@/presentation/providers/AuthProvider';
import { Card } from '@/presentation/components/ui/Card';
import { spacing, radius } from '@/config/theme';

interface SettingItemProps {
  icon: string;
  iconColor?: string;
  title: string;
  subtitle?: string;
  onPress?: () => void;
  rightElement?: React.ReactNode;
  showChevron?: boolean;
  danger?: boolean;
}

function SettingItem({
  icon,
  iconColor,
  title,
  subtitle,
  onPress,
  rightElement,
  showChevron = true,
  danger = false,
}: SettingItemProps) {
  const colors = useThemeColors();

  return (
    <TouchableOpacity
      style={styles.settingItem}
      onPress={onPress}
      disabled={!onPress && !rightElement}
      activeOpacity={0.7}
    >
      <View
        style={[
          styles.settingIcon,
          { backgroundColor: (iconColor || colors.primary[500]) + '15' },
        ]}
      >
        <MaterialIcons
          name={icon as any}
          size={20}
          color={danger ? colors.error : iconColor || colors.primary[500]}
        />
      </View>
      <View style={styles.settingContent}>
        <Text
          style={[
            styles.settingTitle,
            { color: danger ? colors.error : colors.text.primary },
          ]}
        >
          {title}
        </Text>
        {subtitle && (
          <Text style={[styles.settingSubtitle, { color: colors.text.muted }]}>
            {subtitle}
          </Text>
        )}
      </View>
      {rightElement}
      {showChevron && !rightElement && (
        <MaterialIcons name="chevron-right" size={20} color={colors.text.muted} />
      )}
    </TouchableOpacity>
  );
}

export default function SettingsScreen() {
  const colors = useThemeColors();
  const { isDark, toggleTheme } = useTheme();
  const { user, logout } = useAuth();

  const [notificationsEnabled, setNotificationsEnabled] = React.useState(true);
  const [biometricEnabled, setBiometricEnabled] = React.useState(false);

  const handleLogout = () => {
    Alert.alert(
      'Déconnexion',
      'Êtes-vous sûr de vouloir vous déconnecter ?',
      [
        { text: 'Annuler', style: 'cancel' },
        {
          text: 'Déconnexion',
          style: 'destructive',
          onPress: async () => {
            await logout();
            router.replace('/(auth)/login');
          },
        },
      ]
    );
  };

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.title, { color: colors.text.primary }]}>
          Paramètres
        </Text>
      </View>

      {/* Profile Card */}
      <TouchableOpacity
        style={[styles.profileCard, { backgroundColor: colors.surface, borderColor: colors.border }]}
        onPress={() => router.push('/profile' as any)}
        activeOpacity={0.7}
      >
        <View style={[styles.avatar, { backgroundColor: colors.primary[500] }]}>
          <Text style={styles.avatarText}>
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </Text>
        </View>
        <View style={styles.profileInfo}>
          <Text style={[styles.profileName, { color: colors.text.primary }]}>
            {user?.firstName} {user?.lastName}
          </Text>
          <Text style={[styles.profileEmail, { color: colors.text.secondary }]}>
            {user?.email}
          </Text>
        </View>
        <MaterialIcons name="chevron-right" size={24} color={colors.text.muted} />
      </TouchableOpacity>

      {/* Account Section */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text.secondary }]}>
          COMPTE
        </Text>
        <Card variant="outlined" padding="none">
          <SettingItem
            icon="person"
            title="Informations personnelles"
            subtitle="Nom, email, téléphone"
            onPress={() => router.push('/profile/edit' as any)}
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="verified-user"
            title="Vérification KYC"
            subtitle="Compte vérifié"
            iconColor={colors.success}
            onPress={() => {}}
          />
        </Card>
      </View>

      {/* Security Section */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text.secondary }]}>
          SÉCURITÉ
        </Text>
        <Card variant="outlined" padding="none">
          <SettingItem
            icon="lock"
            title="Mot de passe"
            subtitle="Modifier votre mot de passe"
            onPress={() => router.push('/settings/password' as any)}
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="fingerprint"
            title="Biométrie"
            subtitle="Connexion par empreinte/Face ID"
            showChevron={false}
            rightElement={
              <Switch
                value={biometricEnabled}
                onValueChange={setBiometricEnabled}
                trackColor={{ false: colors.gray[300], true: colors.primary[500] }}
                thumbColor="#fff"
              />
            }
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="pin"
            title="Code PIN"
            subtitle="Configurer un code PIN"
            onPress={() => router.push('/settings/pin' as any)}
          />
        </Card>
      </View>

      {/* Preferences Section */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text.secondary }]}>
          PRÉFÉRENCES
        </Text>
        <Card variant="outlined" padding="none">
          <SettingItem
            icon="notifications"
            title="Notifications"
            subtitle={notificationsEnabled ? 'Activées' : 'Désactivées'}
            showChevron={false}
            rightElement={
              <Switch
                value={notificationsEnabled}
                onValueChange={setNotificationsEnabled}
                trackColor={{ false: colors.gray[300], true: colors.primary[500] }}
                thumbColor="#fff"
              />
            }
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon={isDark ? 'dark-mode' : 'light-mode'}
            title="Mode sombre"
            subtitle={isDark ? 'Activé' : 'Désactivé'}
            showChevron={false}
            rightElement={
              <Switch
                value={isDark}
                onValueChange={toggleTheme}
                trackColor={{ false: colors.gray[300], true: colors.primary[500] }}
                thumbColor="#fff"
              />
            }
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="language"
            title="Langue"
            subtitle="Français"
            onPress={() => {}}
          />
        </Card>
      </View>

      {/* Support Section */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text.secondary }]}>
          SUPPORT
        </Text>
        <Card variant="outlined" padding="none">
          <SettingItem
            icon="help"
            title="Centre d'aide"
            onPress={() => {}}
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="chat"
            title="Contacter le support"
            onPress={() => {}}
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="description"
            title="Conditions d'utilisation"
            onPress={() => {}}
          />
          <View style={[styles.divider, { backgroundColor: colors.divider }]} />
          <SettingItem
            icon="privacy-tip"
            title="Politique de confidentialité"
            onPress={() => {}}
          />
        </Card>
      </View>

      {/* Logout */}
      <View style={styles.section}>
        <Card variant="outlined" padding="none">
          <SettingItem
            icon="logout"
            title="Déconnexion"
            danger
            showChevron={false}
            onPress={handleLogout}
          />
        </Card>
      </View>

      {/* App Version */}
      <View style={styles.footer}>
        <Text style={[styles.version, { color: colors.text.muted }]}>
          WillBank v1.0.0
        </Text>
        <Text style={[styles.copyright, { color: colors.text.muted }]}>
          © 2024 WillBank. Tous droits réservés.
        </Text>
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
  header: {
    paddingHorizontal: spacing.lg,
    marginBottom: spacing.lg,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
  },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: spacing.lg,
    padding: spacing.md,
    borderRadius: radius.lg,
    borderWidth: 1,
    marginBottom: spacing.lg,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  avatarText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: '700',
  },
  profileInfo: {
    flex: 1,
  },
  profileName: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 2,
  },
  profileEmail: {
    fontSize: 14,
  },
  section: {
    marginBottom: spacing.lg,
    paddingHorizontal: spacing.lg,
  },
  sectionTitle: {
    fontSize: 12,
    fontWeight: '600',
    marginBottom: spacing.sm,
    letterSpacing: 0.5,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: spacing.md,
  },
  settingIcon: {
    width: 40,
    height: 40,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  settingContent: {
    flex: 1,
  },
  settingTitle: {
    fontSize: 16,
    fontWeight: '500',
  },
  settingSubtitle: {
    fontSize: 12,
    marginTop: 2,
  },
  divider: {
    height: 1,
    marginLeft: spacing.md + 40 + spacing.md,
  },
  footer: {
    alignItems: 'center',
    paddingVertical: spacing.lg,
  },
  version: {
    fontSize: 14,
    marginBottom: spacing.xs,
  },
  copyright: {
    fontSize: 12,
  },
});
