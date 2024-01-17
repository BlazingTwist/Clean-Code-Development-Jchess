import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/src/components/ui/dialog";
import { AlertCircle } from "lucide-react";

export default function Alert({
    title,
    descrption,
    open,
    onOpenChange,
}: {
    title: string;
    descrption: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
}) {
    return (
        <Dialog defaultOpen={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        <span className="flex items-end gap-3">
                            <AlertCircle />
                            {title}
                        </span>
                    </DialogTitle>
                    <DialogDescription className="py-2">{descrption}</DialogDescription>
                </DialogHeader>
            </DialogContent>
        </Dialog>
    );
}

