// ============================================
// WILLBANK - Register Screen
// ============================================

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  TouchableOpacity,
} from 'react-native';
import { router } from 'expo-router';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useThemeColors } from '@/presentation/providers/ThemeProvider';
import { Button } from '@/presentation/components/ui/Button';
import { Input } from '@/presentation/components/ui/Input';
import { spacing, radius } from '@/config/theme';

export default function RegisterScreen() {
  const colors = useThemeColors();
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const updateField = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleRegister = async () => {
    const { firstName, lastName, email, phone, password, confirmPassword } = formData;

    if (!firstName || !lastName || !email || !phone || !password) {
      setError('Veuillez remplir tous les champs');
      return;
    }

    if (password !== confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      return;
    }

    if (password.length < 6) {
      setError('Le mot de passe doit contenir au moins 6 caractères');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      // TODO: Call register API
      await new Promise(resolve => setTimeout(resolve, 2000));
      router.replace('/(tabs)');
    } catch (err) {
      setError('Erreur lors de la création du compte');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        style={[styles.container, { backgroundColor: colors.background }]}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <TouchableOpacity
            style={[styles.backButton, { backgroundColor: colors.surface }]}
            onPress={() => router.back()}
          >
            <MaterialIcons name="arrow-back" size={24} color={colors.text.primary} />
          </TouchableOpacity>
          
          <Text style={[styles.title, { color: colors.text.primary }]}>
            Créer un compte
          </Text>
          <Text style={[styles.subtitle, { color: colors.text.secondary }]}>
            Rejoignez WillBank en quelques étapes
          </Text>
        </View>

        {/* Form */}
        <View style={styles.form}>
          {error ? (
            <View style={[styles.errorContainer, { backgroundColor: colors.error + '15' }]}>
              <Text style={[styles.errorText, { color: colors.error }]}>{error}</Text>
            </View>
          ) : null}

          <View style={styles.row}>
            <View style={styles.halfInput}>
              <Input
                label="Prénom"
                placeholder="Jean"
                value={formData.firstName}
                onChangeText={(v) => updateField('firstName', v)}
                leftIcon="person"
              />
            </View>
            <View style={styles.halfInput}>
              <Input
                label="Nom"
                placeholder="Dupont"
                value={formData.lastName}
                onChangeText={(v) => updateField('lastName', v)}
              />
            </View>
          </View>

          <Input
            label="Email"
            placeholder="exemple@email.com"
            value={formData.email}
            onChangeText={(v) => updateField('email', v)}
            keyboardType="email-address"
            autoCapitalize="none"
            leftIcon="email"
          />

          <Input
            label="Téléphone"
            placeholder="+237 6 XX XX XX XX"
            value={formData.phone}
            onChangeText={(v) => updateField('phone', v)}
            keyboardType="phone-pad"
            leftIcon="phone"
          />

          <Input
            label="Mot de passe"
            placeholder="••••••••"
            value={formData.password}
            onChangeText={(v) => updateField('password', v)}
            secureTextEntry
            leftIcon="lock"
            hint="Minimum 6 caractères"
          />

          <Input
            label="Confirmer le mot de passe"
            placeholder="••••••••"
            value={formData.confirmPassword}
            onChangeText={(v) => updateField('confirmPassword', v)}
            secureTextEntry
            leftIcon="lock"
          />

          <Button
            title="Créer mon compte"
            onPress={handleRegister}
            loading={isLoading}
            fullWidth
            size="lg"
            style={{ marginTop: spacing.md }}
          />
        </View>

        {/* Footer */}
        <View style={styles.footer}>
          <Text style={[styles.footerText, { color: colors.text.secondary }]}>
            Déjà un compte ?{' '}
          </Text>
          <TouchableOpacity onPress={() => router.back()}>
            <Text style={[styles.footerLink, { color: colors.primary[500] }]}>
              Se connecter
            </Text>
          </TouchableOpacity>
        </View>

        {/* Terms */}
        <Text style={[styles.terms, { color: colors.text.muted }]}>
          En créant un compte, vous acceptez nos{' '}
          <Text style={{ color: colors.primary[500] }}>Conditions d'utilisation</Text>
          {' '}et notre{' '}
          <Text style={{ color: colors.primary[500] }}>Politique de confidentialité</Text>
        </Text>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flexGrow: 1,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xxl,
    paddingBottom: spacing.xl,
  },
  header: {
    marginBottom: spacing.xl,
  },
  backButton: {
    width: 44,
    height: 44,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.lg,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    marginBottom: spacing.xs,
  },
  subtitle: {
    fontSize: 16,
  },
  form: {
    flex: 1,
  },
  row: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  halfInput: {
    flex: 1,
  },
  errorContainer: {
    padding: spacing.md,
    borderRadius: radius.md,
    marginBottom: spacing.md,
  },
  errorText: {
    fontSize: 14,
    textAlign: 'center',
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: spacing.xl,
  },
  footerText: {
    fontSize: 14,
  },
  footerLink: {
    fontSize: 14,
    fontWeight: '600',
  },
  terms: {
    fontSize: 12,
    textAlign: 'center',
    marginTop: spacing.lg,
    lineHeight: 18,
  },
});
