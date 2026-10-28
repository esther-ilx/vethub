<script lang="ts">
	import '../app.css';
	import { Toaster } from 'svelte-sonner';
	import { Header, Footer } from '$lib/components/layout';
	import { onMount } from 'svelte';
	import { defaultTheme, restoredTheme, selectTheme, type Theme } from '$lib/theme';

	let { children } = $props();
	let theme = $state<Theme>(defaultTheme);

	onMount(() => {
		theme = restoredTheme();
	});

	function changeTheme(value: Theme) {
		theme = value;
		selectTheme(value);
	}
</script>

<Toaster
	position="top-right"
	theme={theme === 'dark' ? 'dark' : 'light'}
	richColors
	closeButton
	toastOptions={{
		style: `--normal-bg: var(--card); --normal-text: var(--card-foreground); --normal-border: var(--border);
			--success-bg: var(--card); --success-text: var(--success); --success-border: var(--success);
			--warning-bg: var(--card); --warning-text: var(--warning); --warning-border: var(--warning);
			--error-bg: var(--card); --error-text: var(--destructive); --error-border: var(--destructive);
			--info-bg: var(--card); --info-text: var(--primary); --info-border: var(--primary);`
	}}
/>

<div class="flex min-h-screen flex-col bg-background">
	<Header {theme} onThemeChange={changeTheme} />

	<main class="flex-1">
		{@render children()}
	</main>

	<Footer />
</div>
