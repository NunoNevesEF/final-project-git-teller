import {OAuthLinkedAccountListItemDTO} from "@/models/account/OAuthLinkedAccountListItemDTO";
import DropdownMenu from "@/components/utils/DropdownMenu";

interface AccountsDropdownMenu {
    accounts: OAuthLinkedAccountListItemDTO[];
    selectedAccountId: number | null;
    onSelect: (id: number) => void;
}

export function AccountDropdownMenu({accounts, selectedAccountId, onSelect}: AccountsDropdownMenu){
    const items = accounts.map(account => ({
        id: account.id,
        label: account.providerId
    }))

    return (<DropdownMenu items={items} selectedId={selectedAccountId} onSelect={(id) => onSelect(id)}/>)
}