<script lang="ts">
	import { isTheme, themes, type Theme } from '$lib/theme';

	let { theme, onselect }: { theme: Theme; onselect: (theme: Theme) => void } = $props();
</script>

<div class="flex shrink-0 items-center gap-1.5">
	<label for="theme-selector" class="text-sm font-medium">Theme</label>
	<select
		id="theme-selector"
		value={theme}
		onchange={(event) => {
			const value = event.currentTarget.value;
			if (isTheme(value)) onselect(value);
		}}
		class="h-9 rounded-md border border-input bg-background px-1.5 text-sm text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
	>
		{#each themes as option (option)}
			<option value={option}>{option[0].toUpperCase() + option.slice(1)}</option>
		{/each}
	</select>
</div>
