<script lang="ts">
	import { onMount } from 'svelte';
	import { getPets } from '$lib/api/pet/PetController';
	import { getOwners } from '$lib/api/owner/OwnerController';
	import type { OwnerResponse, PetResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import * as Table from '$lib/components/ui/table';
	import { PawPrint, Search } from 'lucide-svelte';

	let pets = $state<PetResponse[]>([]);
	let owners = $state<OwnerResponse[]>([]);
	let loading = $state(true);
	let loadInProgress = false;
	let loadFailed = $state(false);
	let searchQuery = $state('');

	let ownersById = $derived(new Map(owners.map((owner) => [owner.id, owner])));
	let filteredPets = $derived.by(() => {
		const query = searchQuery.trim().toLowerCase();
		return query ? pets.filter((pet) => pet.name?.toLowerCase().includes(query)) : pets;
	});

	async function loadPets() {
		if (loadInProgress) return;
		loadInProgress = true;
		loading = true;
		loadFailed = false;
		try {
			// Wait for both requests to finish before allowing a retry.
			const [petsResult, ownersResult] = await Promise.allSettled([getPets(), getOwners()]);
			if (petsResult.status === 'fulfilled' && ownersResult.status === 'fulfilled') {
				pets = petsResult.value;
				owners = ownersResult.value;
			} else {
				loadFailed = true;
			}
		} finally {
			loading = false;
			loadInProgress = false;
		}
	}

	function ownerName(ownerId: number): string {
		const owner = ownersById.get(ownerId);
		if (!owner) return 'Unknown owner';
		return [owner.firstName, owner.lastName].filter((name) => name?.trim()).join(' ').trim() || 'Unknown owner';
	}

	function formatDate(dateStr: string | undefined): string {
		if (!dateStr) return 'Unknown';
		const date = new Date(dateStr);
		if (Number.isNaN(date.getTime())) return 'Unknown';
		return date.toLocaleDateString('en-US', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}

	function hasValidId(id: number | undefined): boolean {
		return typeof id === 'number' && Number.isInteger(id) && id > 0;
	}

	onMount(() => {
		void loadPets();
	});
</script>

<svelte:head>
	<title>Pets | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8 flex items-center gap-3">
		<div class="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
			<PawPrint class="h-6 w-6 text-primary" />
		</div>
		<div>
			<h1 class="text-2xl font-bold text-foreground">Pets</h1>
			<p class="text-sm text-muted-foreground">Find a patient without looking up their owner first</p>
		</div>
	</div>

	<div class="mb-6 max-w-md">
		<label for="pet-search" class="mb-2 block text-sm font-medium">Search pets</label>
		<div class="relative">
			<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
			<Input id="pet-search" type="search" placeholder="Search by pet name..." bind:value={searchQuery} class="pl-10" />
		</div>
	</div>

	<div class="sr-only" role="status" aria-live="polite" aria-atomic="true">
		{#if loading}
			Loading pets...
		{:else if !loadFailed}
			{#if pets.length === 0}No pets registered yet. {:else if filteredPets.length === 0}No pets found matching "{searchQuery.trim()}". {/if}
			Showing {filteredPets.length} of {pets.length} pets
		{/if}
	</div>

	{#if loading}
		<div class="card p-12 text-center">
			<div class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" aria-hidden="true"></div>
			<p class="text-muted-foreground">Loading pets...</p>
		</div>
	{:else if loadFailed}
		<div class="card p-8 text-center">
			<p class="mb-4 text-muted-foreground" role="alert">Unable to load pets. Please try again.</p>
			<Button onclick={loadPets}>Retry</Button>
		</div>
	{:else}
		{#if pets.length === 0}
			<div class="card p-12 text-center">
				<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" aria-hidden="true" />
				<p class="text-muted-foreground">No pets registered yet</p>
			</div>
		{:else if filteredPets.length === 0}
			<div class="card p-12 text-center">
				<PawPrint class="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" aria-hidden="true" />
				<p class="break-words text-muted-foreground">No pets found matching "{searchQuery.trim()}"</p>
			</div>
		{:else}
			<div class="card min-w-0 overflow-hidden">
				<Table.Root class="min-w-[600px]">
					<Table.Header>
						<Table.Row>
							<Table.Head scope="col">Pet name</Table.Head>
							<Table.Head scope="col">Type</Table.Head>
							<Table.Head scope="col">Birth date</Table.Head>
							<Table.Head scope="col">Owner</Table.Head>
							<Table.Head scope="col" class="w-[100px]">Action</Table.Head>
						</Table.Row>
					</Table.Header>
					<Table.Body>
						{#each filteredPets as pet}
							<Table.Row class="hover:bg-muted/50">
								<Table.Cell class="font-medium">{pet.name?.trim() || 'Unknown'}</Table.Cell>
								<Table.Cell>{pet.type?.name?.trim() || 'Unknown'}</Table.Cell>
								<Table.Cell>{formatDate(pet.birthDate)}</Table.Cell>
								<Table.Cell>{ownerName(pet.ownerId)}</Table.Cell>
								<Table.Cell>
									{#if hasValidId(pet.ownerId) && hasValidId(pet.id)}
										<Button variant="ghost" size="sm" href="/owners/{pet.ownerId}/pets/{pet.id}" aria-label="View {pet.name?.trim() || 'Unknown pet'}, {ownerName(pet.ownerId)}, pet {pet.id}">
											View
										</Button>
									{:else}
										<span class="text-sm text-muted-foreground">Unavailable</span>
									{/if}
								</Table.Cell>
							</Table.Row>
						{/each}
					</Table.Body>
				</Table.Root>
			</div>
		{/if}
		<p class="mt-4 text-sm text-muted-foreground">
			Showing {filteredPets.length} of {pets.length} pets
		</p>
	{/if}
</div>
