package tonius.neiintegration.buildcraft;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import tonius.neiintegration.RecipeHandlerBase;
import tonius.neiintegration.Utils;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IFlexibleRecipeViewable;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;

public class RecipeHandlerAssemblyTable extends RecipeHandlerBase {
    
    private static final int[][] INPUTS = { { 0, 0 }, { 1, 0 }, { 2, 0 }, { 0, 1 }, { 1, 1 }, { 2, 1 }, { 0, 2 }, { 1, 2 }, { 2, 2 }, { 0, 3 }, { 1, 3 }, { 2, 3 } };
    private static Class<? extends GuiContainer> guiClass;
    
    @Override
    public void prepare() {
        guiClass = Utils.getClass("buildcraft.silicon.gui.GuiAssemblyTable");
    }
    
    public class CachedAssemblyTableRecipe extends CachedBaseRecipe {
        
        public List<PositionedStack> inputs = new ArrayList<PositionedStack>();
        public PositionedStack output;
        public int energy;
        
        public CachedAssemblyTableRecipe(IFlexibleRecipeViewable recipe, boolean generatePermutations) {
            this.setIngredients((List<Object>) recipe.getInputs());
            this.output = new PositionedStack(recipe.getOutput(), 129, 8);
            this.energy = recipe.getEnergyCost();
            
            if (generatePermutations) {
                this.generatePermutations();
            }
        }
        
        public CachedAssemblyTableRecipe(IFlexibleRecipeViewable recipe) {
            this(recipe, false);
        }
        
        public void setIngredients(List<Object> inputs) {
            int i = 0;
            for (Object o : inputs) {
                if (i >= INPUTS.length) {
                    return;
                }
                this.inputs.add(new PositionedStack(o, 3 + INPUTS[i][0] * 18, 8 + INPUTS[i][1] * 18, false));
                i++;
            }
        }
        
        @Override
        public List<PositionedStack> getIngredients() {
            return this.getCycledIngredients(RecipeHandlerAssemblyTable.this.cycleticks / 20, this.inputs);
        }
        
        @Override
        public PositionedStack getResult() {
            return this.output;
        }
        
        public void generatePermutations() {
            for (PositionedStack p : this.inputs) {
                p.generatePermutations();
            }
        }
        
    }
    
    @Override
    public String getRecipeName() {
        return "Assembly Table";
    }
    
    @Override
    public String getRecipeID() {
        return "buildcraft.assemblyTable";
    }
    
    @Override
    public String getGuiTexture() {
        return "buildcraft:textures/gui/assembly_table.png";
    }
    
    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(89, 7, 6, 72), this.getRecipeID()));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return guiClass;
    }
    
    @Override
    public int recipiesPerPage() {
        return 1;
    }
    
    @Override
    public void drawBackground(int recipe) {
        this.changeToGuiTexture();
        GuiDraw.drawTexturedModalRect(0, 5, 5, 33, 166, 76);
    }
    
    @Override
    public void drawExtras(int recipe) {
        this.drawProgressBar(90, 7, 176, 17, 4, 71, 100, 3);
        GuiDraw.drawStringC(((CachedAssemblyTableRecipe) this.arecipes.get(recipe)).energy + " RF", 93, 84, 0x808080, false);
    }
    
    @Override
    public void loadAllRecipes() {
        for (IFlexibleRecipe<ItemStack> recipe : BuildcraftRecipeRegistry.assemblyTable.getRecipes()) {
            if (recipe instanceof IFlexibleRecipeViewable) {
                ItemStack output = (ItemStack) ((IFlexibleRecipeViewable) recipe).getOutput();
                if (!(output.getItem() instanceof IFacadeItem)) {
                    this.arecipes.add(new CachedAssemblyTableRecipe((IFlexibleRecipeViewable) recipe, true));
                }
            }
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (IFlexibleRecipe<ItemStack> recipe : BuildcraftRecipeRegistry.assemblyTable.getRecipes()) {
            if (recipe instanceof IFlexibleRecipeViewable) {
                IFlexibleRecipeViewable recipeViewable = (IFlexibleRecipeViewable) recipe;
                ItemStack output = (ItemStack) recipeViewable.getOutput();
                if (output.stackTagCompound != null && NEIServerUtils.areStacksSameType(output, result) || output.stackTagCompound == null && NEIServerUtils.areStacksSameTypeCrafting(output, result)) {
                    this.arecipes.add(new CachedAssemblyTableRecipe((IFlexibleRecipeViewable) recipe, true));
                }
            }
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (IFlexibleRecipe<ItemStack> recipe : BuildcraftRecipeRegistry.assemblyTable.getRecipes()) {
            if (recipe instanceof IFlexibleRecipeViewable) {
                CachedAssemblyTableRecipe crecipe = new CachedAssemblyTableRecipe((IFlexibleRecipeViewable) recipe);
                if (crecipe.contains(crecipe.inputs, ingredient)) {
                    crecipe.generatePermutations();
                    crecipe.setIngredientPermutation(crecipe.inputs, ingredient);
                    this.arecipes.add(crecipe);
                }
            }
        }
    }
    
}
