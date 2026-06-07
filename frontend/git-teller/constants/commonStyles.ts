import { StyleSheet } from 'react-native';
import { colors, fontSize, radius, spacing } from './theme';

export const commonStyles = StyleSheet.create({
    screen: {
        flex: 1,
        padding: spacing.lg,
        backgroundColor: colors.background,
    },
    centered: {
        justifyContent: 'center',
        alignItems: 'center',
    },
    fullWidth: {
        width: '20%',
    },
    formTitle: {
        fontSize: fontSize.xl,
        fontWeight: '700',
        marginBottom: spacing.xl,
        color: colors.textPrimary,
    },
    optionGroup: {
        flexDirection: 'row',
        gap: spacing.sm,
        marginBottom: spacing.lg,
        width: '100%',
        justifyContent: 'center',
    },
    optionButton: {
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.md,
        borderRadius: radius.sm,
        backgroundColor: colors.neutralButton,
        alignItems: 'center',
    },
    optionButtonActive: {
        backgroundColor: colors.primary,
    },
    optionButtonText: {
        fontSize: fontSize.sm,
        fontWeight: '600',
        color: colors.textMuted,
    },
    optionButtonTextActive: {
        color: colors.primaryText,
    },
    inputSection: {
        width: '100%',
        marginBottom: spacing.lg,
        gap: spacing.sm,
        maxWidth: 300,
        alignSelf: 'center',
    },
    pageTitle: {
        fontSize: 26,
        fontWeight: '700',
        marginBottom: 8,
        textAlign: 'center',
        color: '#111827',
    },
    pageSubtitle: {
        fontSize: 14,
        color: '#6b7280',
        marginBottom: 18,
        textAlign: 'center',
    },
    dangerButton: {
        marginTop: 22,
        alignSelf: 'center',
        paddingHorizontal: 14,
        paddingVertical: 10,
        borderRadius: 8,
        backgroundColor: '#ef4444',
    },
    dangerButtonText: {
        color: '#fff',
        fontWeight: '600',
    },
    root: {
        flex: 1,
    },
    topLeftActionButton: {
        position: 'absolute',
        top: 12,
        left: 12,
        zIndex: 10,
        paddingVertical: 8,
        paddingHorizontal: 12,
        borderRadius: 8,
        backgroundColor: '#1f2937',
    },
    topLeftActionButtonText: {
        color: '#fff',
        fontWeight: '600',
    },
    buttonDisabled: {
        opacity: 0.7,
    },
    errorText: {
        color: 'red',
        marginBottom: 8,
        textAlign: 'center',
    },
    authInput: {
        width: '100%',
        borderWidth: 1,
        borderColor: '#ddd',
        paddingVertical: 10,
        paddingHorizontal: 12,
        borderRadius: 8,
        marginBottom: 10,
        backgroundColor: '#fff',
    },
    primaryButton: {
        backgroundColor: '#007AFF',
        paddingVertical: 10,
        paddingHorizontal: 12,
        borderRadius: 8,
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 8,
    },
    primaryButtonText: {
        color: '#fff',
        fontWeight: '700',
        fontSize: 14,
    },
    authTitle: {
        fontSize: 22,
        fontWeight: '700',
        marginBottom: 18,
        textAlign: 'center',
        color: '#111827',
    },
    authFormContainer: {
        width: '100%',
        maxWidth: 280,
        alignSelf: 'center',
    },
    repoItem: {
        padding: spacing.md,
        marginBottom: spacing.md,
        borderRadius: radius.md,
        backgroundColor: '#f3f4f6',
        borderLeftWidth: 4,
        borderLeftColor: '#007AFF',
    },
    repoName: {
        fontSize: fontSize.sm,
        fontWeight: '700',
        color: colors.textPrimary,
        marginBottom: spacing.xs,
    },
    repoDesc: {
        fontSize: fontSize.sm,
        color: colors.textMuted,
        marginBottom: spacing.sm,
        lineHeight: 18,
    },
    repoMeta: {
        fontSize: fontSize.sm,
        color: colors.textMuted,
        fontWeight: '500',
    },
    reposList: {
        width: '100%',
        flex: 1,
    },
    analyzeButton: {
        backgroundColor: '#007AFF',
        paddingVertical: 10,
        paddingHorizontal: 16,
        borderRadius: 8,
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 10,
    },
    analyzeButtonText: {
        color: '#fff',
        fontWeight: '600',
        fontSize: 14,
    },
    analyzeButtonsRow: {
        flexDirection: 'row',
        gap: spacing.sm,
        marginTop: 10,
    },
    analyzeButtonSecondary: {
        backgroundColor: '#1f2937',
    },


});

