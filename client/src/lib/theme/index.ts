export const themes = ['light', 'dark', 'playful'] as const;
export type Theme = (typeof themes)[number];

export const themeStorageKey = 'vethub.theme';
export const defaultTheme: Theme = 'light';

export function isTheme(value: string | null | undefined): value is Theme {
	return themes.some((theme) => theme === value);
}

// Called only in the browser, after app.html has restored the root theme.
export function restoredTheme(): Theme {
	const value = document.documentElement.dataset.theme;
	return isTheme(value) ? value : defaultTheme;
}

export function selectTheme(theme: Theme): void {
	document.documentElement.dataset.theme = theme;
	try {
		localStorage.setItem(themeStorageKey, theme);
	} catch {
		// The selection remains active for this page session without storage.
	}
}
