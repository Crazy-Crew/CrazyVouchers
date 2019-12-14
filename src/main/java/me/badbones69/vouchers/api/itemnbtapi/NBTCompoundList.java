package me.badbones69.vouchers.api.itemnbtapi;

import me.badbones69.vouchers.api.itemnbtapi.utils.MinecraftVersion;
import me.badbones69.vouchers.api.itemnbtapi.utils.nmsmappings.ClassWrapper;
import me.badbones69.vouchers.api.itemnbtapi.utils.nmsmappings.ReflectionMethod;
import org.apache.commons.lang.NotImplementedException;

/**
 * {@link NBTListCompound} implementation for NBTLists
 *
 * @author tr7zw
 *
 */
public class NBTCompoundList extends NBTList<NBTListCompound> {
	
	protected NBTCompoundList(NBTCompound owner, String name, NBTType type, Object list) {
		super(owner, name, type, list);
	}
	
	/**
	 * Adds a new Compound to the end of the List and returns it.
	 *
	 * @return The added {@link NBTListCompound}
	 */
	public NBTListCompound addCompound() {
		try {
			Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
			if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
				ReflectionMethod.LIST_ADD.run(listObject, size(), compound);
			}else {
				ReflectionMethod.LEGACY_LIST_ADD.run(listObject, compound);
			}
			return new NBTListCompound(this, compound);
		}catch(Exception ex) {
			throw new NbtApiException(ex);
		}
	}
	
	/**
	 * Adds a new Compound to the end of the List.
	 *
	 * @param empty This has to be null!
	 * @return True, if compound was added
	 */
	@Override
	public boolean add(NBTListCompound empty) {
		if(empty != null) {
			throw new NotImplementedException("You need to pass null! ListCompounds from other lists won't work.");
		}
		try {
			Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
			if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
				ReflectionMethod.LIST_ADD.run(listObject, 0, compound);
			}else {
				ReflectionMethod.LEGACY_LIST_ADD.run(listObject, compound);
			}
			return true;
		}catch(Exception ex) {
			throw new NbtApiException(ex);
		}
	}
	
	@Override
	public void add(int index, NBTListCompound element) {
		if(element != null) {
			throw new NotImplementedException("You need to pass null! ListCompounds from other lists won't work.");
		}
		try {
			Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
			if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
				ReflectionMethod.LIST_ADD.run(listObject, index, compound);
			}else {
				ReflectionMethod.LEGACY_LIST_ADD.run(listObject, compound);
			}
		}catch(Exception ex) {
			throw new NbtApiException(ex);
		}
	}
	
	@Override
	public NBTListCompound get(int index) {
		try {
			Object compound = ReflectionMethod.LIST_GET_COMPOUND.run(listObject, index);
			return new NBTListCompound(this, compound);
		}catch(Exception ex) {
			throw new NbtApiException(ex);
		}
	}
	
	@Override
	public NBTListCompound set(int index, NBTListCompound element) {
		throw new NotImplementedException("This method doesn't work in the ListCompound context.");
	}
	
	@Override
	protected Object asTag(NBTListCompound object) {
		return null;
	}
	
}
